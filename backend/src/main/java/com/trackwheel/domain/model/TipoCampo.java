package com.trackwheel.domain.model;

/** Tipos suportados pelo motor de campos dinamicos (renderizados pelo front). */
public enum TipoCampo {
    TEXTO,
    TEXTO_LONGO,
    NUMERO,
    DECIMAL,
    MOEDA,
    DATA,
    BOOLEANO,
    SELECT,
    MULTI_SELECT,
    FOTO,
    ASSINATURA,
    CHECKLIST;

    public boolean exigeOpcoes() {
        return this == SELECT || this == MULTI_SELECT || this == CHECKLIST;
    }
}
