package com.trackwheel.domain.model;

/**
 * Ramo de atuacao da oficina. Define o catalogo de campos dinamicos carregado nas OS.
 *
 * A ordem aqui e a ordem que o onboarding mostra (`GET /api/templates/ramos` usa
 * `values()`), entao ela vai do mais comum para o mais especializado. Nenhum ramo
 * e privilegiado no produto: o sistema atende oficina de qualquer especialidade.
 * Persistencia e por `name()`, nunca por ordinal — reordenar aqui e seguro.
 */
public enum Ramo {
    MECANICA_GERAL("Mecanica geral"),
    SUSPENSAO_FREIOS("Suspensao e freios"),
    TROCA_OLEO("Troca de oleo"),
    INJECAO_ELETRONICA("Injecao eletronica"),
    ELETRICA("Eletrica"),
    AR_CONDICIONADO("Ar-condicionado"),
    PNEUS_ALINHAMENTO("Pneus e alinhamento"),
    FUNILARIA_PINTURA("Funilaria e pintura"),
    RADIADOR("Radiador"),
    OUTRO("Outro");

    private final String rotulo;

    Ramo(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
