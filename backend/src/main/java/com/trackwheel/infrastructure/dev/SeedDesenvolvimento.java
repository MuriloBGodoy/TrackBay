package com.trackwheel.infrastructure.dev;

import com.trackwheel.domain.model.Cliente;
import com.trackwheel.domain.model.Combustivel;
import com.trackwheel.domain.model.Endereco;
import com.trackwheel.domain.model.FormaPagamento;
import com.trackwheel.domain.model.ItemPeca;
import com.trackwheel.domain.model.ItemServico;
import com.trackwheel.domain.model.Oficina;
import com.trackwheel.domain.model.OrdemServico;
import com.trackwheel.domain.model.OrigemPeca;
import com.trackwheel.domain.model.Pagamento;
import com.trackwheel.domain.model.Papel;
import com.trackwheel.domain.model.Produto;
import com.trackwheel.domain.model.LocalOficina;
import com.trackwheel.domain.model.Ramo;
import com.trackwheel.domain.model.StatusOS;
import com.trackwheel.domain.model.TipoObjeto;
import com.trackwheel.domain.model.TipoPessoa;
import com.trackwheel.domain.model.TipoVeiculo;
import com.trackwheel.domain.model.Usuario;
import com.trackwheel.domain.model.Veiculo;
import com.trackwheel.domain.repository.UsuarioRepository;
import com.trackwheel.domain.service.ClienteService;
import com.trackwheel.domain.service.EstoqueService;
import com.trackwheel.domain.service.OficinaService;
import com.trackwheel.domain.service.OrdemServicoService;
import com.trackwheel.domain.service.VeiculoService;
import com.trackwheel.security.DevDados;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Popula o app no perfil dev com uma oficina generica ja funcional.
 * O objetivo e abrir o front e ver o produto rodando sem cadastrar nada.
 */
@Component
@Profile("dev")
public class SeedDesenvolvimento implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDesenvolvimento.class);

    private final OficinaService oficinaService;
    private final UsuarioRepository usuarioRepository;
    private final ClienteService clienteService;
    private final VeiculoService veiculoService;
    private final EstoqueService estoqueService;
    private final OrdemServicoService osService;

    public SeedDesenvolvimento(OficinaService oficinaService,
                               UsuarioRepository usuarioRepository,
                               ClienteService clienteService,
                               VeiculoService veiculoService,
                               EstoqueService estoqueService,
                               OrdemServicoService osService) {
        this.oficinaService = oficinaService;
        this.usuarioRepository = usuarioRepository;
        this.clienteService = clienteService;
        this.veiculoService = veiculoService;
        this.estoqueService = estoqueService;
        this.osService = osService;
    }

    @Override
    public void run(String... args) {
        Usuario dono = new Usuario();
        dono.setUid(DevDados.UID_DONO);
        dono.setNome("Murilo (dono)");
        dono.setEmail(DevDados.EMAIL_DONO);
        dono.setPapel(Papel.OWNER);

        Oficina oficina = new Oficina();
        oficina.setNomeFantasia("Oficina Track Bay");
        oficina.setRazaoSocial("Track Bay Servicos Automotivos LTDA");
        oficina.setCnpj("11222333000181");
        // Oficina generica de proposito: o seed nao deve sugerir que o sistema e
        // de um ramo so. Dois ramos ja provam o catalogo de campos trocando.
        oficina.setRamos(List.of(Ramo.MECANICA_GERAL, Ramo.SUSPENSAO_FREIOS));
        oficina.setTelefone("1134567890");
        oficina.setWhatsapp("11987654321");
        oficina.setEmail(DevDados.EMAIL_DONO);
        oficina.setHorarioFuncionamento("Seg a Sex 8h-18h, Sab 8h-12h");
        oficina.setEndereco(new Endereco("01310100", "Av. Paulista", "1000", null,
                "Bela Vista", "Sao Paulo", "SP"));

        oficina = oficinaService.onboarding(oficina, dono);
        String oficinaId = oficina.getId();

        Usuario mecanico = new Usuario();
        mecanico.setUid("dev-uid-mecanico");
        mecanico.setNome("Joao (mecanico)");
        mecanico.setEmail("joao@oficinatrackbay.com.br");
        mecanico.setPapel(Papel.MECHANIC);
        mecanico.setOficinaId(oficinaId);
        usuarioRepository.salvar(mecanico);

        Cliente pf = new Cliente();
        pf.setTipoPessoa(TipoPessoa.FISICA);
        pf.setNome("Carlos Souza");
        pf.setTelefone("11991234567");
        pf.setWhatsapp("11991234567");
        pf.setEmail("carlos@email.com");
        pf.setConsentimentoLgpd(true);
        Cliente.DadosPF dadosPF = new Cliente.DadosPF();
        dadosPF.setCpf("52998224725");
        dadosPF.setDataNascimento(LocalDate.of(1985, 4, 12));
        pf.setDadosPF(dadosPF);
        pf.setEndereco(new Endereco("04101000", "Rua Vergueiro", "500", "ap 22",
                "Paraiso", "Sao Paulo", "SP"));
        pf = clienteService.salvar(oficinaId, pf);

        Cliente pj = new Cliente();
        pj.setTipoPessoa(TipoPessoa.JURIDICA);
        pj.setNome("Transportadora Rapida");
        pj.setTelefone("1133224455");
        pj.setConsentimentoLgpd(true);
        Cliente.DadosPJ dadosPJ = new Cliente.DadosPJ();
        dadosPJ.setCnpj("04252011000110");
        dadosPJ.setRazaoSocial("Transportadora Rapida LTDA");
        dadosPJ.setContatoResponsavel(new Cliente.DadosPJ.ContatoResponsavel(
                "Ana Lima", "Gerente de frota", "11988887777"));
        dadosPJ.getCondicoesComerciais().setPrazoPagamentoDias(30);
        dadosPJ.getCondicoesComerciais().setLimiteCredito(new BigDecimal("10000"));
        dadosPJ.getCondicoesComerciais().setFaturamentoMensal(true);
        pj.setDadosPJ(dadosPJ);
        pj = clienteService.salvar(oficinaId, pj);

        Veiculo gol = new Veiculo();
        gol.setClienteId(pf.getId());
        gol.setPlaca("ABC1234");
        gol.setMarca("Volkswagen");
        gol.setModelo("Gol");
        gol.setVersao("1.6 Trendline");
        gol.setAnoFabricacao(2015);
        gol.setAnoModelo(2016);
        gol.setCor("Prata");
        gol.setCombustivel(Combustivel.FLEX);
        gol.setMotorizacao("1.6");
        gol.setKmAtual(98000);
        gol.setTipoVeiculo(TipoVeiculo.CARRO);
        gol.setChassi("9BWZZZ377VT004251");
        gol = veiculoService.salvar(oficinaId, gol);

        Veiculo caminhao = new Veiculo();
        caminhao.setClienteId(pj.getId());
        caminhao.setPlaca("BRA2E19");
        caminhao.setMarca("Mercedes-Benz");
        caminhao.setModelo("Accelo 1016");
        caminhao.setAnoFabricacao(2019);
        caminhao.setAnoModelo(2020);
        caminhao.setCor("Branco");
        caminhao.setCombustivel(Combustivel.DIESEL);
        caminhao.setKmAtual(320000);
        caminhao.setTipoVeiculo(TipoVeiculo.CAMINHAO);
        veiculoService.salvar(oficinaId, caminhao);

        Produto pastilha = produto(oficinaId, "FRE-001", "7891234567895", "Pastilha de freio dianteira Gol G5",
                "Cobreq", "Freios", "88.00", "179.90", "8", "2", "Prateleira A1");
        Produto oleo = produto(oficinaId, "OLE-002", "7891234567901", "Oleo motor 5W30 sintetico 1L",
                "Mobil", "Fluidos", "28.90", "59.90", "3", "5", "Prateleira B2");
        produto(oficinaId, "FIL-003", "7891234567918", "Filtro de oleo",
                "Tecfil", "Filtros", "18.00", "42.90", "12", "4", "Prateleira A3");

        // Duas OS de ramos diferentes de proposito: e o que mostra o catalogo de
        // campos dinamicos trocando de cara entre uma OS e outra.
        OrdemServico os = new OrdemServico();
        os.setClienteId(pf.getId());
        os.setVeiculoId(gol.getId());
        os.setKmEntrada(98000);
        os.setRamo(Ramo.MECANICA_GERAL);
        os.setReclamacaoCliente("Barulho de batida seca ao passar em lombada e revisao atrasada.");
        os.setDiagnosticoTecnico("Coxim do amortecedor gasto. Revisao de 100 mil km pendente.");
        os.setCamposDinamicos(Map.of(
                "sistema_afetado", List.of("Suspensao", "Motor"),
                "ruidos", List.of("Batida seca"),
                "quando_ocorre", "Constante",
                "revisao_preventiva", List.of("Oleo do motor", "Filtro de oleo", "Filtro de ar", "Velas"),
                "fluidos_trocados", List.of("Oleo do motor"),
                "km_proxima_revisao", 108000,
                "observacoes_tecnicas", "Recomendado trocar o par de coxins na proxima revisao."
        ));

        ItemServico mao = new ItemServico();
        mao.setDescricao("Revisao completa e troca de oleo");
        mao.setTipo("Mao de obra");
        mao.setValorUnitario(new BigDecimal("180.00"));
        mao.setQuantidade(BigDecimal.ONE);
        mao.setMecanicoId(mecanico.getId());
        mao.setMecanicoNome(mecanico.getNome());
        os.getItensServico().add(mao);

        ItemServico teste = new ItemServico();
        teste.setDescricao("Diagnostico de ruido na suspensao");
        teste.setTipo("Diagnostico");
        teste.setValorUnitario(new BigDecimal("60.00"));
        teste.setQuantidade(BigDecimal.ONE);
        os.getItensServico().add(teste);

        os.getItensPeca().add(itemPeca(pastilha, "1"));
        os.getItensPeca().add(itemPeca(oleo, "2"));

        Pagamento.Parcela pix = new Pagamento.Parcela();
        pix.setForma(FormaPagamento.PIX);
        pix.setValor(new BigDecimal("200.00"));
        pix.setValorRecebido(new BigDecimal("200.00"));
        Pagamento.Parcela credito = new Pagamento.Parcela();
        credito.setForma(FormaPagamento.CREDITO);
        credito.setValor(new BigDecimal("279.80"));
        credito.setNumeroParcelas(2);
        credito.setTaxaMaquininha(new BigDecimal("3.5"));
        os.getPagamento().getParcelas().add(pix);
        os.getPagamento().getParcelas().add(credito);

        os.setPrevisaoEntrega(Instant.now().plus(1, ChronoUnit.DAYS));

        os = osService.criar(oficinaId, os, dono);
        osService.mudarStatus(oficinaId, os.getId(), StatusOS.APROVADA, "Cliente aprovou na hora", dono);
        osService.mudarStatus(oficinaId, os.getId(), StatusOS.EM_EXECUCAO, null, dono);
        // Ja subiu na rampa: o patio da tela inicial mostra o lugar real.
        osService.moverLocal(oficinaId, os.getId(), LocalOficina.ELEVADOR, null, dono);

        OrdemServico orcamento = new OrdemServico();
        orcamento.setClienteId(pj.getId());
        orcamento.setVeiculoId(caminhao.getId());
        orcamento.setKmEntrada(320000);
        orcamento.setRamo(Ramo.SUSPENSAO_FREIOS);
        orcamento.setReclamacaoCliente("Freio com curso longo e pedal baixo na frota.");
        orcamento.setCamposDinamicos(Map.of(
                "componente", List.of("Pastilha", "Disco", "Fluido de freio"),
                "espessura_pastilha_dianteira", 3.2,
                "estado_disco", "Sulcado",
                "necessita_alinhamento", true
        ));
        ItemServico limpeza = new ItemServico();
        limpeza.setDescricao("Troca de pastilhas e retifica dos discos");
        limpeza.setValorUnitario(new BigDecimal("250.00"));
        limpeza.setQuantidade(BigDecimal.ONE);
        orcamento.getItensServico().add(limpeza);
        osService.criar(oficinaId, orcamento, dono);

        // Terceira OS: peca que chegou sozinha, sem o carro. Prova que a OS nao
        // depende de veiculo e que o patio sabe mostrar objeto sem placa.
        OrdemServico avulsa = new OrdemServico();
        avulsa.setTipoObjeto(TipoObjeto.PECA);
        avulsa.setObjetoDescricao("Radiador Gol G5 (cliente trouxe so a peca)");
        avulsa.setClienteId(pf.getId());
        avulsa.setRamo(Ramo.MECANICA_GERAL);
        avulsa.setReclamacaoCliente("Peca vazando pela colmeia.");
        avulsa.setPrevisaoEntrega(Instant.now().plus(3, ChronoUnit.DAYS));
        // sistema_afetado e obrigatorio no template de mecanica geral — vale para
        // peca avulsa igual: o catalogo do ramo nao sabe se veio carro junto.
        avulsa.setCamposDinamicos(Map.of("sistema_afetado", List.of("Arrefecimento")));
        ItemServico reparo = new ItemServico();
        reparo.setDescricao("Reparo e teste de estanqueidade");
        reparo.setValorUnitario(new BigDecimal("180.00"));
        reparo.setQuantidade(BigDecimal.ONE);
        avulsa.getItensServico().add(reparo);
        avulsa = osService.criar(oficinaId, avulsa, dono);
        osService.moverLocal(oficinaId, avulsa.getId(), LocalOficina.OUTRO, "Bancada da funilaria", dono);

        log.info("=== Seed dev pronto ===");
        log.info("Oficina: {} (id {})", oficina.getNomeFantasia(), oficinaId);
        log.info("Usuario dev: {} — use o header X-Dev-User para trocar de usuario", DevDados.EMAIL_DONO);
        log.info("Mecanico: {}", mecanico.getEmail());
        log.info("Veiculos: ABC-1234 (Gol) e BRA2E19 (caminhao)");
    }

    private Produto produto(String oficinaId, String codigo, String ean, String nome, String marca,
                            String categoria, String custo, String venda, String estoque,
                            String minimo, String local) {
        Produto p = new Produto();
        p.setCodigoInterno(codigo);
        p.setCodigoBarras(ean);
        p.setNome(nome);
        p.setMarca(marca);
        p.setCategoria(categoria);
        p.setPrecoCusto(new BigDecimal(custo));
        p.setPrecoVenda(new BigDecimal(venda));
        p.setEstoqueAtual(new BigDecimal(estoque));
        p.setEstoqueMinimo(new BigDecimal(minimo));
        p.setLocalizacao(local);
        return estoqueService.salvarProduto(oficinaId, p);
    }

    private ItemPeca itemPeca(Produto produto, String quantidade) {
        ItemPeca item = new ItemPeca();
        item.setProdutoId(produto.getId());
        item.setDescricao(produto.getNome());
        item.setCodigo(produto.getCodigoInterno());
        item.setQuantidade(new BigDecimal(quantidade));
        item.setValorUnitario(produto.getPrecoVenda());
        item.setOrigem(OrigemPeca.ESTOQUE_PROPRIO);
        return item;
    }
}
