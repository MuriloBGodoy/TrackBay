package com.trackwheel.domain.repository;

import com.trackwheel.domain.model.MovimentacaoEstoque;

import java.util.List;

public interface MovimentacaoEstoqueRepository {

    MovimentacaoEstoque salvar(MovimentacaoEstoque movimentacao);

    List<MovimentacaoEstoque> listarPorProduto(String oficinaId, String produtoId);

    List<MovimentacaoEstoque> listarPorOficina(String oficinaId, int limite);
}
