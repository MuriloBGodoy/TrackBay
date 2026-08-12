package com.trackwheel.domain.model;

/** Ramo de atuacao da oficina. Define o catalogo de campos dinamicos carregado nas OS. */
public enum Ramo {
    RADIADOR("Radiador"),
    MECANICA_GERAL("Mecanica geral"),
    FUNILARIA_PINTURA("Funilaria e pintura"),
    ELETRICA("Eletrica"),
    SUSPENSAO_FREIOS("Suspensao e freios"),
    AR_CONDICIONADO("Ar-condicionado"),
    TROCA_OLEO("Troca de oleo"),
    PNEUS_ALINHAMENTO("Pneus e alinhamento"),
    INJECAO_ELETRONICA("Injecao eletronica"),
    OUTRO("Outro");

    private final String rotulo;

    Ramo(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
