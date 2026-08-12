package com.trackwheel.infrastructure.firestore;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.trackwheel.domain.model.Oficina;
import com.trackwheel.domain.repository.OficinaRepository;
import com.trackwheel.domain.validation.DocumentoValidator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Tenants na colecao raiz "oficinas". Os dados da oficina ficam no documento;
 * clientes, veiculos, OS etc. vivem em subcolecoes do mesmo documento.
 */
@Repository
@Profile("!dev")
public class OficinaRepositoryFirestore implements OficinaRepository {

    private final Firestore db;
    private final ConversorFirestore conversor;

    public OficinaRepositoryFirestore(Firestore db, ConversorFirestore conversor) {
        this.db = db;
        this.conversor = conversor;
    }

    @Override
    public Oficina salvar(Oficina oficina) {
        if (oficina.getId() == null || oficina.getId().isBlank()) {
            oficina.setId(UUID.randomUUID().toString());
        }
        // CNPJ guardado so com digitos para a busca por igualdade funcionar.
        if (oficina.getCnpj() != null && !oficina.getCnpj().isBlank()) {
            oficina.setCnpj(DocumentoValidator.somenteDigitos(oficina.getCnpj()));
        }
        FirestoreStore.esperar(db.collection("oficinas").document(oficina.getId())
                .set(conversor.paraMapa(oficina)));
        return oficina;
    }

    @Override
    public Optional<Oficina> buscarPorId(String id) {
        DocumentSnapshot doc = FirestoreStore.esperar(db.collection("oficinas").document(id).get());
        if (!doc.exists() || doc.getData() == null) {
            return Optional.empty();
        }
        return Optional.of(conversor.paraEntidade(doc.getData(), Oficina.class));
    }

    @Override
    public Optional<Oficina> buscarPorCnpj(String cnpj) {
        String alvo = DocumentoValidator.somenteDigitos(cnpj);
        return FirestoreStore.esperar(db.collection("oficinas")
                        .whereEqualTo("cnpj", alvo).limit(1).get())
                .getDocuments().stream()
                .findFirst()
                .map(d -> conversor.paraEntidade(d.getData(), Oficina.class));
    }

    /** Transacao: dois atendentes simultaneos nunca recebem o mesmo numero de OS. */
    @Override
    public int proximoNumeroOS(String oficinaId) {
        DocumentReference ref = db.collection("oficinas").document(oficinaId);
        Long numero = FirestoreStore.esperar(db.runTransaction(transacao -> {
            DocumentSnapshot doc = transacao.get(ref).get();
            if (!doc.exists()) {
                throw new IllegalArgumentException("Oficina nao encontrada: " + oficinaId);
            }
            Object atual = doc.get("config.proximoNumeroOS");
            long n = atual instanceof Number num ? num.longValue() : 1L;
            transacao.update(ref, "config.proximoNumeroOS", n + 1);
            return n;
        }));
        return numero.intValue();
    }
}
