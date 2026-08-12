package com.trackwheel.domain.repository;

import com.trackwheel.domain.model.Fornecedor;

import java.util.List;
import java.util.Optional;

public interface FornecedorRepository {

    Fornecedor salvar(Fornecedor fornecedor);

    Optional<Fornecedor> buscarPorId(String oficinaId, String id);

    List<Fornecedor> listarPorOficina(String oficinaId);

    void remover(String oficinaId, String id);
}
