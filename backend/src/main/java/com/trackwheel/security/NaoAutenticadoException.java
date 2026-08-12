package com.trackwheel.security;

/** Token ausente, invalido ou usuario sem oficina. Vira 401 no handler. */
public class NaoAutenticadoException extends RuntimeException {

    public NaoAutenticadoException(String mensagem) {
        super(mensagem);
    }
}
