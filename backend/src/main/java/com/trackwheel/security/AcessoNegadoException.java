package com.trackwheel.security;

/** Usuario autenticado mas sem permissao para a acao. Vira 403 no handler. */
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
