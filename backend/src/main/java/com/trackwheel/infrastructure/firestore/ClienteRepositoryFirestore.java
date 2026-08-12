package com.trackwheel.infrastructure.firestore;

import com.google.cloud.firestore.Firestore;
import com.trackwheel.domain.model.Cliente;
import com.trackwheel.domain.repository.ClienteRepository;
import com.trackwheel.domain.validation.DocumentoValidator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("!dev")
public class ClienteRepositoryFirestore extends FirestoreStore<Cliente> implements ClienteRepository {

    public ClienteRepositoryFirestore(Firestore db, ConversorFirestore conversor) {
        super(db, conversor, "clientes", Cliente.class);
    }

    @Override
    protected String id(Cliente c) {
        return c.getId();
    }

    @Override
    protected void atribuirId(Cliente c, String id) {
        c.setId(id);
    }

    @Override
    protected String oficinaId(Cliente c) {
        return c.getOficinaId();
    }

    @Override
    public Cliente salvar(Cliente cliente) {
        return persistir(cliente);
    }

    @Override
    public Optional<Cliente> buscarPorId(String oficinaId, String id) {
        return porId(oficinaId, id);
    }

    @Override
    public List<Cliente> listarPorOficina(String oficinaId) {
        return doTenant(oficinaId).stream()
                .sorted(Comparator.comparing(Cliente::getNome, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    @Override
    public List<Cliente> buscar(String oficinaId, String termo) {
        return filtrar(oficinaId, c -> contemTermo(c, termo,
                Cliente::getNome,
                Cliente::getTelefone,
                Cliente::getWhatsapp,
                Cliente::getEmail,
                Cliente::documento));
    }

    @Override
    public Optional<Cliente> buscarPorDocumento(String oficinaId, String documento) {
        String alvo = DocumentoValidator.somenteDigitos(documento);
        return primeiro(oficinaId, c -> alvo.equals(c.documento()));
    }

    @Override
    public void remover(String oficinaId, String id) {
        excluir(oficinaId, id);
    }
}
