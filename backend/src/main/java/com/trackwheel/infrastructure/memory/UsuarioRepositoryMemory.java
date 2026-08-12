package com.trackwheel.infrastructure.memory;

import com.trackwheel.domain.model.Usuario;
import com.trackwheel.domain.repository.UsuarioRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile("dev")
public class UsuarioRepositoryMemory extends MemoryStore<Usuario> implements UsuarioRepository {

    @Override
    protected String id(Usuario u) {
        return u.getId();
    }

    @Override
    protected void atribuirId(Usuario u, String id) {
        u.setId(id);
    }

    @Override
    protected String oficinaId(Usuario u) {
        return u.getOficinaId();
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        return persistir(usuario);
    }

    @Override
    public Optional<Usuario> buscarPorId(String id) {
        return Optional.ofNullable(raw().get(id));
    }

    @Override
    public Optional<Usuario> buscarPorUid(String uid) {
        return raw().values().stream().filter(u -> uid.equals(u.getUid())).findFirst();
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return raw().values().stream()
                .filter(u -> email != null && email.equalsIgnoreCase(u.getEmail()))
                .findFirst();
    }

    @Override
    public List<Usuario> listarPorOficina(String oficinaId) {
        return doTenant(oficinaId);
    }

    @Override
    public void remover(String id) {
        raw().remove(id);
    }
}
