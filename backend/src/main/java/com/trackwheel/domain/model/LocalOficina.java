package com.trackwheel.domain.model;

/**
 * Onde o objeto da OS esta fisicamente, agora.
 *
 * Nao confundir com {@link StatusOS}: status e a etapa do trabalho, local e o
 * lugar. Um carro APROVADA pode estar no patio esperando vaga no elevador, e um
 * EM_EXECUCAO pode estar na ducha. Sao eixos independentes — foi por isso que
 * viraram campos separados em vez de mais valores no status.
 *
 * {@link #OUTRO} existe porque cada oficina tem um canto com nome proprio; nesse
 * caso a OS guarda o texto livre em {@code localDetalhe}.
 */
public enum LocalOficina {
    PATIO("Patio"),
    BOX("Box"),
    ELEVADOR("Elevador"),
    DUCHA("Ducha"),
    TESTE("Em teste"),
    EXTERNO("Em terceiro"),
    OUTRO("Outro");

    private final String rotulo;

    LocalOficina(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    /** Só OUTRO aceita (e exige) descricao livre. */
    public boolean exigeDetalhe() {
        return this == OUTRO;
    }
}
