package com.trackwheel.domain.model;

/** Papel do usuario dentro da oficina (tenant). */
public enum Papel {
    /** Dono: gerencia tudo, incluindo assinatura. */
    OWNER,
    /** Gerente: OS, estoque, relatorios. */
    MANAGER,
    /** Atendente: cria OS e cadastros. */
    ATTENDANT,
    /** Mecanico: so ve e atualiza as OS atribuidas a ele. */
    MECHANIC
}
