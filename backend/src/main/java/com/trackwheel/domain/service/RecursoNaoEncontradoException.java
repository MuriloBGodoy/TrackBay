package com.trackwheel.domain.service;

/** Recurso inexistente ou de outro tenant. Vira 404 no handler da API. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, String id) {
        super(recurso + " nao encontrado: " + id);
    }
}
