package com.trackwheel.domain.model;

public enum FormaPagamento {
    DINHEIRO,
    PIX,
    DEBITO,
    CREDITO,
    BOLETO,
    TRANSFERENCIA,
    /** Faturado para cliente PJ / frotista: agrupado numa fatura mensal. */
    FATURADO,
    CHEQUE;

    public boolean aceitaParcelas() {
        return this == CREDITO || this == BOLETO || this == CHEQUE;
    }
}
