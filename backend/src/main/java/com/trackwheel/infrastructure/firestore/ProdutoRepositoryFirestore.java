package com.trackwheel.infrastructure.firestore;

import com.google.cloud.firestore.Firestore;
import com.trackwheel.domain.model.Produto;
import com.trackwheel.domain.repository.ProdutoRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("!dev")
public class ProdutoRepositoryFirestore extends FirestoreStore<Produto> implements ProdutoRepository {

    public ProdutoRepositoryFirestore(Firestore db, ConversorFirestore conversor) {
        super(db, conversor, "produtos", Produto.class);
    }

    @Override
    protected String id(Produto p) {
        return p.getId();
    }

    @Override
    protected void atribuirId(Produto p, String id) {
        p.setId(id);
    }

    @Override
    protected String oficinaId(Produto p) {
        return p.getOficinaId();
    }

    @Override
    public Produto salvar(Produto produto) {
        return persistir(produto);
    }

    @Override
    public Optional<Produto> buscarPorId(String oficinaId, String id) {
        return porId(oficinaId, id);
    }

    @Override
    public Optional<Produto> buscarPorCodigoBarras(String oficinaId, String codigoBarras) {
        if (codigoBarras == null || codigoBarras.isBlank()) {
            return Optional.empty();
        }
        return primeiroPorCampo(oficinaId, "codigoBarras", codigoBarras);
    }

    @Override
    public List<Produto> listarPorOficina(String oficinaId) {
        return doTenant(oficinaId).stream()
                .sorted(Comparator.comparing(Produto::getNome, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    @Override
    public List<Produto> buscar(String oficinaId, String termo) {
        return filtrar(oficinaId, p -> contemTermo(p, termo,
                Produto::getNome,
                Produto::getCodigoInterno,
                Produto::getCodigoBarras,
                Produto::getMarca,
                Produto::getCategoria));
    }

    /** Compara estoque atual x minimo — dois campos, o Firestore nao faz isso em query. */
    @Override
    public List<Produto> listarEstoqueBaixo(String oficinaId) {
        return filtrar(oficinaId, p -> p.isAtivo() && p.isEstoqueBaixo());
    }

    @Override
    public void remover(String oficinaId, String id) {
        excluir(oficinaId, id);
    }
}
