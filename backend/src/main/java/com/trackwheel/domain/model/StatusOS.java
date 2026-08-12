package com.trackwheel.domain.model;

import java.util.Set;

/**
 * Ciclo de vida da Ordem de Servico.
 * As transicoes permitidas ficam no proprio enum para que a regra viva no dominio.
 */
public enum StatusOS {
    ORCAMENTO,
    APROVADA,
    EM_EXECUCAO,
    AGUARDANDO_PECA,
    PRONTA,
    ENTREGUE,
    CANCELADA;

    public Set<StatusOS> proximosPermitidos() {
        return switch (this) {
            case ORCAMENTO -> Set.of(APROVADA, CANCELADA);
            case APROVADA -> Set.of(EM_EXECUCAO, CANCELADA);
            case EM_EXECUCAO -> Set.of(AGUARDANDO_PECA, PRONTA, CANCELADA);
            case AGUARDANDO_PECA -> Set.of(EM_EXECUCAO, CANCELADA);
            case PRONTA -> Set.of(ENTREGUE, EM_EXECUCAO, CANCELADA);
            case ENTREGUE, CANCELADA -> Set.of();
        };
    }

    public boolean podeIrPara(StatusOS destino) {
        return proximosPermitidos().contains(destino);
    }

    /** Status terminais nao aceitam mais alteracao de itens. */
    public boolean isFinal() {
        return this == ENTREGUE || this == CANCELADA;
    }
}
