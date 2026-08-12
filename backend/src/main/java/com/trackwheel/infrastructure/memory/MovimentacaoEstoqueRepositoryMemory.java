package com.trackwheel.infrastructure.memory;

import com.trackwheel.domain.model.MovimentacaoEstoque;
import com.trackwheel.domain.repository.MovimentacaoEstoqueRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;

@Repository
@Profile("dev")
public class MovimentacaoEstoqueRepositoryMemory extends MemoryStore<MovimentacaoEstoque>
        implements MovimentacaoEstoqueRepository {

    @Override
    protected String id(MovimentacaoEstoque m) {
        return m.getId();
    }

    @Override
    protected void atribuirId(MovimentacaoEstoque m, String id) {
        m.setId(id);
    }

    @Override
    protected String oficinaId(MovimentacaoEstoque m) {
        return m.getOficinaId();
    }

    @Override
    public MovimentacaoEstoque salvar(MovimentacaoEstoque movimentacao) {
        return persistir(movimentacao);
    }

    @Override
    public List<MovimentacaoEstoque> listarPorProduto(String oficinaId, String produtoId) {
        return filtrar(oficinaId, m -> produtoId.equals(m.getProdutoId())).stream()
                .sorted(Comparator.comparing(MovimentacaoEstoque::getEm).reversed())
                .toList();
    }

    @Override
    public List<MovimentacaoEstoque> listarPorOficina(String oficinaId, int limite) {
        return doTenant(oficinaId).stream()
                .sorted(Comparator.comparing(MovimentacaoEstoque::getEm).reversed())
                .limit(limite)
                .toList();
    }
}
