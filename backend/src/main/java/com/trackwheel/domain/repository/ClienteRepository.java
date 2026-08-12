package com.trackwheel.domain.repository;

import com.trackwheel.domain.model.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository {

    Cliente salvar(Cliente cliente);

    Optional<Cliente> buscarPorId(String oficinaId, String id);

    List<Cliente> listarPorOficina(String oficinaId);

    /** Busca por nome, documento ou telefone. */
    List<Cliente> buscar(String oficinaId, String termo);

    Optional<Cliente> buscarPorDocumento(String oficinaId, String documento);

    void remover(String oficinaId, String id);
}
