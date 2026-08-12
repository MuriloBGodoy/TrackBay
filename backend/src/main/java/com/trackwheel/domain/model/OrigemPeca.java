package com.trackwheel.domain.model;

public enum OrigemPeca {
    /** Sai do estoque da oficina: gera baixa quando a OS conclui. */
    ESTOQUE_PROPRIO,
    /** Comprada para a OS: nao movimenta estoque. */
    COMPRADA,
    /** Trazida pelo cliente: nao movimenta estoque nem entra no custo. */
    FORNECIDA_CLIENTE;

    public boolean movimentaEstoque() {
        return this == ESTOQUE_PROPRIO;
    }
}
