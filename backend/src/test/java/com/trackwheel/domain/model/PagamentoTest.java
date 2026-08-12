package com.trackwheel.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PagamentoTest {

    private Pagamento.Parcela parcela(FormaPagamento forma, String valor, String recebido) {
        Pagamento.Parcela p = new Pagamento.Parcela();
        p.setForma(forma);
        p.setValor(new BigDecimal(valor));
        p.setValorRecebido(new BigDecimal(recebido));
        return p;
    }

    @Test
    @DisplayName("pagamento dividido: R$200 no PIX + R$300 em 2x no credito soma R$500")
    void pagamentoDividido() {
        Pagamento pg = new Pagamento();
        pg.getParcelas().add(parcela(FormaPagamento.PIX, "200.00", "200.00"));
        Pagamento.Parcela credito = parcela(FormaPagamento.CREDITO, "300.00", "300.00");
        credito.setNumeroParcelas(2);
        pg.getParcelas().add(credito);
        pg.recalcularStatus();

        assertEquals(new BigDecimal("500.00"), pg.getTotalPrevisto());
        assertEquals(new BigDecimal("500.00"), pg.getTotalRecebido());
        assertEquals(StatusPagamento.PAGO, pg.getStatus());
    }

    @Test
    @DisplayName("recebeu so uma parte: status PARCIAL e saldo aberto")
    void pagamentoParcial() {
        Pagamento pg = new Pagamento();
        pg.getParcelas().add(parcela(FormaPagamento.PIX, "200.00", "200.00"));
        pg.getParcelas().add(parcela(FormaPagamento.CREDITO, "300.00", "0"));
        pg.recalcularStatus();

        assertEquals(StatusPagamento.PARCIAL, pg.getStatus());
        assertEquals(new BigDecimal("300.00"), pg.getSaldo());
    }

    @Test
    @DisplayName("nada recebido: PENDENTE")
    void pagamentoPendente() {
        Pagamento pg = new Pagamento();
        pg.getParcelas().add(parcela(FormaPagamento.BOLETO, "500.00", "0"));
        pg.recalcularStatus();

        assertEquals(StatusPagamento.PENDENTE, pg.getStatus());
    }

    @Test
    @DisplayName("parcela vencida em aberto marca o pagamento como ATRASADO")
    void pagamentoAtrasado() {
        Pagamento pg = new Pagamento();
        Pagamento.Parcela p = parcela(FormaPagamento.BOLETO, "500.00", "100.00");
        p.setVencimento(LocalDate.now().minusDays(5));
        pg.getParcelas().add(p);
        pg.recalcularStatus();

        assertEquals(StatusPagamento.ATRASADO, pg.getStatus());
    }

    @Test
    @DisplayName("taxa da maquininha desconta do liquido")
    void valorLiquidoComTaxa() {
        Pagamento.Parcela p = parcela(FormaPagamento.CREDITO, "1000.00", "1000.00");
        p.setTaxaMaquininha(new BigDecimal("3.5"));

        assertEquals(new BigDecimal("965.00"), p.getValorLiquido());
    }

    @Test
    @DisplayName("sem parcelas o pagamento fica pendente e nao quebra")
    void semParcelas() {
        Pagamento pg = new Pagamento();
        pg.recalcularStatus();

        assertEquals(StatusPagamento.PENDENTE, pg.getStatus());
        assertEquals(new BigDecimal("0.00"), pg.getTotalPrevisto());
    }
}
