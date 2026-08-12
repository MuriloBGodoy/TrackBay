package com.trackwheel.infrastructure.firestore;

import com.google.cloud.firestore.Firestore;
import com.trackwheel.domain.model.Fornecedor;
import com.trackwheel.domain.repository.FornecedorRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile("!dev")
public class FornecedorRepositoryFirestore extends FirestoreStore<Fornecedor> implements FornecedorRepository {

    public FornecedorRepositoryFirestore(Firestore db, ConversorFirestore conversor) {
        super(db, conversor, "fornecedores", Fornecedor.class);
    }

    @Override
    protected String id(Fornecedor f) {
        return f.getId();
    }

    @Override
    protected void atribuirId(Fornecedor f, String id) {
        f.setId(id);
    }

    @Override
    protected String oficinaId(Fornecedor f) {
        return f.getOficinaId();
    }

    @Override
    public Fornecedor salvar(Fornecedor fornecedor) {
        return persistir(fornecedor);
    }

    @Override
    public Optional<Fornecedor> buscarPorId(String oficinaId, String id) {
        return porId(oficinaId, id);
    }

    @Override
    public List<Fornecedor> listarPorOficina(String oficinaId) {
        return doTenant(oficinaId);
    }

    @Override
    public void remover(String oficinaId, String id) {
        excluir(oficinaId, id);
    }
}
