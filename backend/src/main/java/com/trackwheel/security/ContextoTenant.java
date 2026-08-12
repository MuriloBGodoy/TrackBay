package com.trackwheel.security;

import com.trackwheel.domain.model.Usuario;

/**
 * Usuario e tenant da requisicao atual, resolvidos a partir do ID Token.
 * Nunca aceite oficinaId vindo do corpo ou da query: e sempre daqui que ele sai.
 */
public final class ContextoTenant {

    private static final ThreadLocal<Usuario> ATUAL = new ThreadLocal<>();

    private ContextoTenant() {
    }

    public static void definir(Usuario usuario) {
        ATUAL.set(usuario);
    }

    public static Usuario usuario() {
        Usuario usuario = ATUAL.get();
        if (usuario == null) {
            throw new NaoAutenticadoException("Requisicao sem usuario autenticado");
        }
        return usuario;
    }

    public static String oficinaId() {
        return usuario().getOficinaId();
    }

    public static boolean autenticado() {
        return ATUAL.get() != null;
    }

    /** Sempre chamado no finally do filtro: a thread e reaproveitada entre requisicoes. */
    public static void limpar() {
        ATUAL.remove();
    }
}
