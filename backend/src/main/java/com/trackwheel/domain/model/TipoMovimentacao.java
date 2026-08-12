package com.trackwheel.domain.model;

public enum TipoMovimentacao {
    ENTRADA,
    SAIDA,
    AJUSTE,
    DEVOLUCAO;

    /** Sinal aplicado ao estoque atual. AJUSTE define o valor absoluto, por isso zero. */
    public int sinal() {
        return switch (this) {
            case ENTRADA, DEVOLUCAO -> 1;
            case SAIDA -> -1;
            case AJUSTE -> 0;
        };
    }
}
