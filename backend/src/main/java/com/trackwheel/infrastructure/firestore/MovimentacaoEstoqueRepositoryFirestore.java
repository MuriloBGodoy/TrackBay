package com.trackwheel.infrastructure.firestore;

import com.google.cloud.firestore.Firestore;
import com.trackwheel.domain.model.MovimentacaoEstoque;
import com.trackwheel.domain.repository.MovimentacaoEstoqueRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;

@Repository
@Profile("!dev")
public class MovimentacaoEstoqueRepositoryFirestore extends FirestoreStore<MovimentacaoEstoque>
        implements MovimentacaoEstoqueRepository {

    public MovimentacaoEstoqueRepositoryFirestore(Firestore db, ConversorFirestore conversor) {
        super(db, conversor, "movimentacoes", MovimentacaoEstoque.class);
    }

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
        return porCampo(oficinaId, "produtoId", produtoId).stream()
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
