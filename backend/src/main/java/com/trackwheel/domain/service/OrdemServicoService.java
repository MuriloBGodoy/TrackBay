package com.trackwheel.domain.service;

import com.trackwheel.domain.model.Cliente;
import com.trackwheel.domain.model.ItemPeca;
import com.trackwheel.domain.model.Oficina;
import com.trackwheel.domain.model.OrdemServico;
import com.trackwheel.domain.model.Ramo;
import com.trackwheel.domain.model.StatusOS;
import com.trackwheel.domain.model.TemplateCampos;
import com.trackwheel.domain.model.TipoMovimentacao;
import com.trackwheel.domain.model.Usuario;
import com.trackwheel.domain.model.Veiculo;
import com.trackwheel.domain.repository.ClienteRepository;
import com.trackwheel.domain.repository.OficinaRepository;
import com.trackwheel.domain.repository.OrdemServicoRepository;
import com.trackwheel.domain.repository.VeiculoRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.Optional;

/** Orquestra o ciclo de vida da OS: numeracao, campos dinamicos, transicoes e baixa de estoque. */
@Service
public class OrdemServicoService {

    private final OrdemServicoRepository repository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final OficinaRepository oficinaRepository;
    private final TemplateCamposService templateService;
    private final EstoqueService estoqueService;

    public OrdemServicoService(OrdemServicoRepository repository,
                               ClienteRepository clienteRepository,
                               VeiculoRepository veiculoRepository,
                               OficinaRepository oficinaRepository,
                               TemplateCamposService templateService,
                               EstoqueService estoqueService) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
        this.oficinaRepository = oficinaRepository;
        this.templateService = templateService;
        this.estoqueService = estoqueService;
    }

    /** Abre a OS como ORCAMENTO, ja com numero, snapshot de cliente/veiculo e versao do schema. */
    public OrdemServico criar(String oficinaId, OrdemServico os, Usuario autor) {
        os.setOficinaId(oficinaId);

        Cliente cliente = clienteRepository.buscarPorId(oficinaId, os.getClienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", os.getClienteId()));
        Veiculo veiculo = veiculoRepository.buscarPorId(oficinaId, os.getVeiculoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veiculo", os.getVeiculoId()));

        if (!veiculo.getClienteId().equals(cliente.getId())) {
            throw new RegraNegocioException("O veiculo " + veiculo.getPlacaFormatada()
                    + " nao pertence ao cliente " + cliente.getNome());
        }

        // Snapshot: a OS guarda como cliente e veiculo estavam no momento da abertura.
        os.setClienteNome(cliente.getNome());
        os.setVeiculoPlaca(veiculo.getPlaca());
        os.setVeiculoDescricao(veiculo.descricaoCurta());

        Oficina oficina = oficinaRepository.buscarPorId(oficinaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Oficina", oficinaId));

        if (os.getRamo() == null) {
            os.setRamo(oficina.getRamos().isEmpty() ? Ramo.OUTRO : oficina.getRamos().get(0));
        }
        if (!oficina.getRamos().isEmpty() && !oficina.atendeRamo(os.getRamo())) {
            throw new RegraNegocioException("A oficina nao atende o ramo " + os.getRamo());
        }

        // Congela a versao do schema: se o template mudar depois, esta OS continua com a dela.
        Optional<TemplateCampos> template = templateService.buscarAtivo(oficinaId, os.getRamo());
        template.ifPresent(t -> {
            os.setSchemaVersion(t.getVersao());
            templateService.validarPreenchimento(t, os.getCamposDinamicos());
        });

        os.setNumero(gerarNumero(oficinaId));
        os.setStatus(StatusOS.ORCAMENTO);
        os.setDataAbertura(Instant.now());
        os.setCriadoPor(autor.getId());
        os.setAtualizadoPor(autor.getId());

        if (os.getGarantiaDias() == null) {
            os.setGarantiaDias(oficina.getConfig().getGarantiaPadraoDias());
        }
        if (os.getTextoGarantia() == null) {
            os.setTextoGarantia(oficina.getConfig().getTextoGarantiaPadrao());
        }

        veiculo.atualizarKm(os.getKmEntrada());
        veiculoRepository.salvar(veiculo);

        os.getPagamento().recalcularStatus();
        return repository.salvar(os);
    }

    /** Numeracao sequencial por oficina: 2026-0001. */
    private String gerarNumero(String oficinaId) {
        int sequencial = oficinaRepository.proximoNumeroOS(oficinaId);
        return "%d-%04d".formatted(Year.now().getValue(), sequencial);
    }

    public OrdemServico atualizar(String oficinaId, String id, OrdemServico dados, Usuario autor) {
        OrdemServico os = buscarOuFalhar(oficinaId, id);
        if (!os.permiteEdicaoDeItens()) {
            throw new RegraNegocioException("OS " + os.getNumero() + " esta " + os.getStatus()
                    + " e nao aceita mais alteracoes");
        }

        os.setReclamacaoCliente(dados.getReclamacaoCliente());
        os.setDiagnosticoTecnico(dados.getDiagnosticoTecnico());
        os.setItensServico(dados.getItensServico());
        os.setItensPeca(dados.getItensPeca());
        os.setDescontoGeral(dados.getDescontoGeral());
        os.setAcrescimo(dados.getAcrescimo());
        os.setPrevisaoEntrega(dados.getPrevisaoEntrega());
        os.setCamposDinamicos(dados.getCamposDinamicos());
        os.setChecklistEntrada(dados.getChecklistEntrada());
        os.setGarantiaDias(dados.getGarantiaDias());
        os.setGarantiaKm(dados.getGarantiaKm());
        if (dados.getPagamento() != null) {
            os.setPagamento(dados.getPagamento());
        }

        // Revalida contra a versao de schema com que a OS foi criada, nao contra a atual.
        templateService.buscarVersao(oficinaId, os.getRamo(), os.getSchemaVersion())
                .ifPresent(t -> templateService.validarPreenchimento(t, os.getCamposDinamicos()));

        os.getPagamento().recalcularStatus();
        os.setAtualizadoPor(autor.getId());
        os.setAtualizadoEm(Instant.now());
        return repository.salvar(os);
    }

    /**
     * Muda o status respeitando o fluxo. Ao entregar, baixa o estoque das pecas proprias.
     */
    public OrdemServico mudarStatus(String oficinaId, String id, StatusOS destino,
                                    String observacao, Usuario autor) {
        OrdemServico os = buscarOuFalhar(oficinaId, id);
        StatusOS anterior = os.getStatus();

        os.transicionarPara(destino, autor.getId(), autor.getNome(), observacao);

        if (destino == StatusOS.ENTREGUE && anterior != StatusOS.ENTREGUE) {
            baixarEstoque(oficinaId, os, autor);
        }
        return repository.salvar(os);
    }

    /** Baixa automatica ao entregar: so pecas de estoque proprio movimentam. */
    private void baixarEstoque(String oficinaId, OrdemServico os, Usuario autor) {
        for (ItemPeca peca : os.pecasQueBaixamEstoque()) {
            if (peca.getProdutoId() == null) {
                continue;
            }
            estoqueService.movimentar(oficinaId, peca.getProdutoId(), TipoMovimentacao.SAIDA,
                    peca.getQuantidade(), "Consumo na OS " + os.getNumero(), os.getId(),
                    autor.getId(), autor.getNome());
        }
    }

    /** Aprovacao com assinatura do cliente coletada no canvas do mobile. */
    public OrdemServico aprovar(String oficinaId, String id, String assinaturaUrl, Usuario autor) {
        OrdemServico os = buscarOuFalhar(oficinaId, id);
        if (assinaturaUrl != null && !assinaturaUrl.isBlank()) {
            os.setAssinaturaClienteUrl(assinaturaUrl);
            os.setAssinadaEm(Instant.now());
        }
        os.transicionarPara(StatusOS.APROVADA, autor.getId(), autor.getNome(), "Orcamento aprovado");
        return repository.salvar(os);
    }

    public OrdemServico buscarOuFalhar(String oficinaId, String id) {
        return repository.buscarPorId(oficinaId, id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de servico", id));
    }

    public Optional<OrdemServico> buscarPorId(String oficinaId, String id) {
        return repository.buscarPorId(oficinaId, id);
    }

    /** Respeita o papel: mecanico so ve as OS atribuidas a ele. */
    public List<OrdemServico> listar(String oficinaId, Usuario usuario) {
        if (usuario.veApenasOSAtribuidas()) {
            return repository.listarPorMecanico(oficinaId, usuario.getId());
        }
        return repository.listarPorOficina(oficinaId);
    }

    public List<OrdemServico> listarPorStatus(String oficinaId, StatusOS status) {
        return repository.listarPorStatus(oficinaId, status);
    }

    /** Historico completo do veiculo: toda OS daquela placa. */
    public List<OrdemServico> historicoDoVeiculo(String oficinaId, String veiculoId) {
        return repository.listarPorVeiculo(oficinaId, veiculoId);
    }

    public List<OrdemServico> listarPorCliente(String oficinaId, String clienteId) {
        return repository.listarPorCliente(oficinaId, clienteId);
    }

    public List<OrdemServico> listarPorPeriodo(String oficinaId, Instant de, Instant ate) {
        return repository.listarPorPeriodo(oficinaId, de, ate);
    }
}
