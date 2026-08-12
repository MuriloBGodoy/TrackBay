package com.trackwheel.infrastructure.pdf;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import com.trackwheel.domain.model.CampoDinamico;
import com.trackwheel.domain.model.FormaPagamento;
import com.trackwheel.domain.model.ItemPeca;
import com.trackwheel.domain.model.ItemServico;
import com.trackwheel.domain.model.Oficina;
import com.trackwheel.domain.model.OrdemServico;
import com.trackwheel.domain.model.Pagamento;
import com.trackwheel.domain.model.Ramo;
import com.trackwheel.domain.model.StatusOS;
import com.trackwheel.domain.model.TemplateCampos;
import com.trackwheel.domain.model.TipoCampo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrdemServicoPdfTest {

    private final OrdemServicoPdf gerador = new OrdemServicoPdf();

    private Oficina oficina() {
        Oficina o = new Oficina();
        o.setId("of-1");
        o.setNomeFantasia("Radiadores Silva");
        o.setCnpj("11222333000181");
        o.setTelefone("11987654321");
        o.setEmail("contato@radiadoressilva.com.br");
        return o;
    }

    private OrdemServico ordem() {
        OrdemServico os = new OrdemServico();
        os.setNumero("OS-0042");
        os.setClienteNome("Maria Aparecida");
        os.setVeiculoPlaca("BRA2E19");
        os.setVeiculoDescricao("VW Gol 1.6 2019");
        os.setKmEntrada(85000);
        os.setRamo(Ramo.RADIADOR);
        os.setReclamacaoCliente("Carro esquentando no transito");

        ItemServico servico = new ItemServico();
        servico.setDescricao("Limpeza quimica do radiador");
        servico.setValorUnitario(new BigDecimal("150.00"));
        os.getItensServico().add(servico);

        ItemPeca peca = new ItemPeca();
        peca.setDescricao("Aditivo organico");
        peca.setQuantidade(new BigDecimal("2"));
        peca.setValorUnitario(new BigDecimal("45.50"));
        os.getItensPeca().add(peca);

        os.setGarantiaDias(90);
        os.setTextoGarantia("Garantia de 90 dias sobre servicos executados.");
        return os;
    }

    private TemplateCampos template() {
        TemplateCampos t = new TemplateCampos();
        t.setRamo(Ramo.RADIADOR);
        t.setVersao(1);
        t.setCampos(List.of(
                new CampoDinamico("tipo_radiador", "Tipo do radiador", TipoCampo.SELECT, true,
                        List.of("Aluminio", "Cobre"), 1, "Radiador"),
                new CampoDinamico("teve_vazamento", "Teve vazamento", TipoCampo.BOOLEANO, false,
                        null, 2, "Radiador"),
                new CampoDinamico("foto_entrada", "Foto na entrada", TipoCampo.FOTO, false,
                        null, 3, "Radiador")
        ));
        return t;
    }

    private String textoDoPdf(byte[] bytes) throws Exception {
        PdfReader leitor = new PdfReader(bytes);
        StringBuilder texto = new StringBuilder();
        PdfTextExtractor extrator = new PdfTextExtractor(leitor);
        for (int pagina = 1; pagina <= leitor.getNumberOfPages(); pagina++) {
            texto.append(extrator.getTextFromPage(pagina)).append('\n');
        }
        leitor.close();
        return texto.toString();
    }

    @Test
    @DisplayName("gera um PDF valido com oficina, veiculo, itens, totais e garantia")
    void pdfCompleto() throws Exception {
        OrdemServico os = ordem();
        os.getCamposDinamicos().putAll(Map.of(
                "tipo_radiador", "Aluminio",
                "teve_vazamento", true,
                "foto_entrada", "foto.jpg"
        ));

        byte[] bytes = gerador.gerar(oficina(), os, template());

        assertTrue(bytes.length > 500);
        assertEquals("%PDF", new String(bytes, 0, 4));

        String texto = textoDoPdf(bytes);
        assertTrue(texto.contains("Radiadores Silva"));
        assertTrue(texto.contains("OS-0042"));
        assertTrue(texto.contains("ORÇAMENTO"), "OS em orcamento deve sair como ORÇAMENTO");
        assertTrue(texto.contains("Maria Aparecida"));
        assertTrue(texto.contains("BRA2E19"));
        assertTrue(texto.contains("Limpeza quimica do radiador"));
        assertTrue(texto.contains("Aditivo organico"));
        assertTrue(texto.contains("241,00"), "total 150 + 2x45,50 deve aparecer");
        assertTrue(texto.contains("Tipo do radiador".toUpperCase()));
        assertTrue(texto.contains("Aluminio"));
        assertTrue(texto.contains("Sim"), "booleano true deve virar Sim");
        assertFalse(texto.contains("foto.jpg"), "campo FOTO nao entra no PDF");
        assertTrue(texto.contains("90 dias"));
        assertTrue(texto.contains("Assinatura do cliente"), "orcamento tem linha de assinatura");
    }

    @Test
    @DisplayName("OS aprovada sai como ORDEM DE SERVIÇO, sem linha de assinatura")
    void pdfDeOsAprovada() throws Exception {
        OrdemServico os = ordem();
        os.transicionarPara(StatusOS.APROVADA, "u1", "Joao", null);

        Pagamento.Parcela parcela = new Pagamento.Parcela();
        parcela.setForma(FormaPagamento.PIX);
        parcela.setValor(new BigDecimal("241.00"));
        os.getPagamento().getParcelas().add(parcela);

        String texto = textoDoPdf(gerador.gerar(oficina(), os, null));

        assertTrue(texto.contains("ORDEM DE SERVIÇO"));
        assertFalse(texto.contains("Assinatura do cliente"));
        assertTrue(texto.contains("PIX"));
    }

    @Test
    @DisplayName("nao quebra com OS minima: sem template, sem itens, sem pagamento")
    void pdfMinimo() throws Exception {
        OrdemServico os = new OrdemServico();
        os.setNumero("OS-0001");

        Oficina o = new Oficina();
        o.setNomeFantasia("Oficina Minima");

        byte[] bytes = gerador.gerar(o, os, null);
        assertEquals("%PDF", new String(bytes, 0, 4));
        assertTrue(textoDoPdf(bytes).contains("OS-0001"));
    }
}
