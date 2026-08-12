package com.trackwheel.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrdemServicoTest {

    private ItemServico servico(String descricao, String valor, String qtd, String desconto) {
        ItemServico i = new ItemServico();
        i.setDescricao(descricao);
        i.setValorUnitario(new BigDecimal(valor));
        i.setQuantidade(new BigDecimal(qtd));
        i.setDesconto(new BigDecimal(desconto));
        return i;
    }

    private ItemPeca peca(String descricao, String valor, String qtd, OrigemPeca origem) {
        ItemPeca p = new ItemPeca();
        p.setDescricao(descricao);
        p.setValorUnitario(new BigDecimal(valor));
        p.setQuantidade(new BigDecimal(qtd));
        p.setOrigem(origem);
        return p;
    }

    @Test
    @DisplayName("total soma servicos e pecas, aplica desconto geral e acrescimo")
    void calculoDeTotais() {
        OrdemServico os = new OrdemServico();
        os.getItensServico().add(servico("Limpeza quimica do radiador", "150.00", "1", "0"));
        os.getItensServico().add(servico("Mao de obra", "100.00", "2", "20.00"));
        os.getItensPeca().add(peca("Aditivo", "45.50", "2", OrigemPeca.ESTOQUE_PROPRIO));
        os.setDescontoGeral(new BigDecimal("30.00"));
        os.setAcrescimo(new BigDecimal("10.00"));

        assertEquals(new BigDecimal("330.00"), os.getSubtotalServicos());
        assertEquals(new BigDecimal("91.00"), os.getSubtotalPecas());
        assertEquals(new BigDecimal("401.00"), os.getTotal());
    }

    @Test
    @DisplayName("total nunca fica negativo mesmo com desconto maior que o subtotal")
    void totalNaoNegativo() {
        OrdemServico os = new OrdemServico();
        os.getItensServico().add(servico("Servico", "100.00", "1", "0"));
        os.setDescontoGeral(new BigDecimal("500.00"));

        assertEquals(new BigDecimal("0.00"), os.getTotal());
    }

    @Test
    @DisplayName("fluxo feliz: orcamento -> aprovada -> execucao -> pronta -> entregue")
    void fluxoCompleto() {
        OrdemServico os = new OrdemServico();
        os.transicionarPara(StatusOS.APROVADA, "u1", "Murilo", "cliente aprovou por telefone");
        os.transicionarPara(StatusOS.EM_EXECUCAO, "u1", "Murilo", null);
        os.transicionarPara(StatusOS.PRONTA, "u1", "Murilo", null);
        os.transicionarPara(StatusOS.ENTREGUE, "u1", "Murilo", null);

        assertEquals(StatusOS.ENTREGUE, os.getStatus());
        assertEquals(4, os.getHistoricoStatus().size());
        assertNotNull(os.getDataConclusao());
        assertNotNull(os.getDataEntrega());
    }

    @Test
    @DisplayName("historico registra autor e status de origem/destino")
    void historicoAuditavel() {
        OrdemServico os = new OrdemServico();
        os.transicionarPara(StatusOS.APROVADA, "u9", "Joao", "ok");

        OrdemServico.TransicaoStatus t = os.getHistoricoStatus().get(0);
        assertEquals(StatusOS.ORCAMENTO, t.de());
        assertEquals(StatusOS.APROVADA, t.para());
        assertEquals("u9", t.autorId());
        assertEquals("ok", t.observacao());
        assertNotNull(t.em());
    }

    @Test
    @DisplayName("rejeita pular etapa do fluxo")
    void transicaoInvalida() {
        OrdemServico os = new OrdemServico();
        assertThrows(IllegalStateException.class,
                () -> os.transicionarPara(StatusOS.ENTREGUE, "u1", "Murilo", null));
    }

    @Test
    @DisplayName("OS entregue e um estado terminal: nao transiciona nem aceita edicao de itens")
    void estadoTerminal() {
        OrdemServico os = new OrdemServico();
        os.transicionarPara(StatusOS.APROVADA, "u1", "M", null);
        os.transicionarPara(StatusOS.EM_EXECUCAO, "u1", "M", null);
        os.transicionarPara(StatusOS.PRONTA, "u1", "M", null);
        os.transicionarPara(StatusOS.ENTREGUE, "u1", "M", null);

        assertFalse(os.permiteEdicaoDeItens());
        assertThrows(IllegalStateException.class,
                () -> os.transicionarPara(StatusOS.EM_EXECUCAO, "u1", "M", null));
    }

    @Test
    @DisplayName("transicionar para o mesmo status e no-op, sem sujar o historico")
    void mesmoStatusNaoRegistra() {
        OrdemServico os = new OrdemServico();
        os.transicionarPara(StatusOS.ORCAMENTO, "u1", "M", null);

        assertEquals(StatusOS.ORCAMENTO, os.getStatus());
        assertTrue(os.getHistoricoStatus().isEmpty());
    }

    @Test
    @DisplayName("aguardando peca volta para execucao")
    void aguardandoPecaVolta() {
        OrdemServico os = new OrdemServico();
        os.transicionarPara(StatusOS.APROVADA, "u1", "M", null);
        os.transicionarPara(StatusOS.EM_EXECUCAO, "u1", "M", null);
        os.transicionarPara(StatusOS.AGUARDANDO_PECA, "u1", "M", "aguardando colmeia");
        os.transicionarPara(StatusOS.EM_EXECUCAO, "u1", "M", "peca chegou");

        assertEquals(StatusOS.EM_EXECUCAO, os.getStatus());
    }

    @Test
    @DisplayName("so pecas de estoque proprio geram baixa")
    void pecasQueBaixamEstoque() {
        OrdemServico os = new OrdemServico();
        os.getItensPeca().add(peca("Colmeia", "300", "1", OrigemPeca.ESTOQUE_PROPRIO));
        os.getItensPeca().add(peca("Mangueira", "50", "1", OrigemPeca.COMPRADA));
        os.getItensPeca().add(peca("Tampa", "20", "1", OrigemPeca.FORNECIDA_CLIENTE));

        assertEquals(1, os.pecasQueBaixamEstoque().size());
        assertEquals("Colmeia", os.pecasQueBaixamEstoque().get(0).getDescricao());
    }
}
