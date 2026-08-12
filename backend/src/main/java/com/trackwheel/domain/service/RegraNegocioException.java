package com.trackwheel.domain.service;

/** Violacao de regra de negocio. Vira 422 no handler da API (Problem Details). */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
