package com.trackwheel.domain.repository;

import com.trackwheel.domain.model.Produto;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository {

    Produto salvar(Produto produto);

    Optional<Produto> buscarPorId(String oficinaId, String id);

    Optional<Produto> buscarPorCodigoBarras(String oficinaId, String codigoBarras);

    List<Produto> listarPorOficina(String oficinaId);

    /** Busca por nome, codigo interno, codigo de barras ou marca. */
    List<Produto> buscar(String oficinaId, String termo);

    /** Produtos no estoque minimo ou abaixo — alimenta o alerta do dashboard. */
    List<Produto> listarEstoqueBaixo(String oficinaId);

    void remover(String oficinaId, String id);
}
