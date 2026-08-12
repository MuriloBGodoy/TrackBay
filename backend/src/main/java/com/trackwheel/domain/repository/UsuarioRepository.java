package com.trackwheel.domain.repository;

import com.trackwheel.domain.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {

    Usuario salvar(Usuario usuario);

    Optional<Usuario> buscarPorId(String id);

    /** Resolve o usuario a partir do uid do token — e daqui que sai o tenant. */
    Optional<Usuario> buscarPorUid(String uid);

    Optional<Usuario> buscarPorEmail(String email);

    List<Usuario> listarPorOficina(String oficinaId);

    void remover(String id);
}
