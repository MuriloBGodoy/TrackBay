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
import com.trackwheel.domain.model.Ramo;
import com.trackwheel.domain.model.StatusOS;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Popula o app no perfil dev com uma oficina de radiador ja funcional.
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
        oficina.setNomeFantasia("Radiadores Track Wheel");
        oficina.setRazaoSocial("Track Wheel Radiadores LTDA");
        oficina.setCnpj("11222333000181");
        oficina.setRamos(List.of(Ramo.RADIADOR, Ramo.MECANICA_GERAL));
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
        mecanico.setEmail("joao@oficinatrackwheel.com.br");
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

        Produto colmeia = produto(oficinaId, "RAD-001", "7891234567895", "Colmeia de radiador Gol G5",
                "Universal", "Radiador", "180.00", "320.00", "8", "2", "Prateleira A1");
        Produto aditivo = produto(oficinaId, "ADT-002", "7891234567901", "Aditivo radiador rosa 1L",
                "Paraflu", "Fluidos", "18.90", "39.90", "3", "5", "Prateleira B2");
        produto(oficinaId, "MAN-003", "7891234567918", "Mangueira superior radiador",
                "Cofap", "Radiador", "35.00", "79.90", "12", "4", "Prateleira A3");

        OrdemServico os = new OrdemServico();
        os.setClienteId(pf.getId());
        os.setVeiculoId(gol.getId());
        os.setKmEntrada(98000);
        os.setRamo(Ramo.RADIADOR);
        os.setReclamacaoCliente("Carro esquentando no transito e perdendo agua.");
        os.setDiagnosticoTecnico("Vazamento na colmeia e tampa com vedacao ruim.");
        os.setCamposDinamicos(Map.of(
                "tipo_radiador", "Aluminio/plastico",
                "houve_superaquecimento", true,
                "temperatura_atingida", 110,
                "houve_vazamento", true,
                "local_vazamento", "Colmeia",
                "teste_pressao", 1.2,
                "estado_tampa", "Vedacao ruim",
                "servico_executado", List.of("Troca de colmeia", "Troca de tampa"),
                "tipo_aditivo", "Organico (OAT) rosa"
        ));

        ItemServico mao = new ItemServico();
        mao.setDescricao("Remocao e instalacao do radiador");
        mao.setTipo("Mao de obra");
        mao.setValorUnitario(new BigDecimal("180.00"));
        mao.setQuantidade(BigDecimal.ONE);
        mao.setMecanicoId(mecanico.getId());
        mao.setMecanicoNome(mecanico.getNome());
        os.getItensServico().add(mao);

        ItemServico teste = new ItemServico();
        teste.setDescricao("Teste de pressao e estanqueidade");
        teste.setTipo("Diagnostico");
        teste.setValorUnitario(new BigDecimal("60.00"));
        teste.setQuantidade(BigDecimal.ONE);
        os.getItensServico().add(teste);

        os.getItensPeca().add(itemPeca(colmeia, "1"));
        os.getItensPeca().add(itemPeca(aditivo, "2"));

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

        os = osService.criar(oficinaId, os, dono);
        osService.mudarStatus(oficinaId, os.getId(), StatusOS.APROVADA, "Cliente aprovou na hora", dono);
        osService.mudarStatus(oficinaId, os.getId(), StatusOS.EM_EXECUCAO, null, dono);

        OrdemServico orcamento = new OrdemServico();
        orcamento.setClienteId(pj.getId());
        orcamento.setVeiculoId(caminhao.getId());
        orcamento.setKmEntrada(320000);
        orcamento.setRamo(Ramo.RADIADOR);
        orcamento.setReclamacaoCliente("Revisao preventiva do sistema de arrefecimento.");
        orcamento.setCamposDinamicos(Map.of(
                "tipo_radiador", "Cobre/latao",
                "servico_executado", List.of("Limpeza quimica")
        ));
        ItemServico limpeza = new ItemServico();
        limpeza.setDescricao("Limpeza quimica do radiador");
        limpeza.setValorUnitario(new BigDecimal("250.00"));
        limpeza.setQuantidade(BigDecimal.ONE);
        orcamento.getItensServico().add(limpeza);
        osService.criar(oficinaId, orcamento, dono);

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
