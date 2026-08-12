package com.trackwheel.infrastructure.firestore;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.trackwheel.domain.model.Usuario;
import com.trackwheel.domain.repository.UsuarioRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Usuarios na colecao raiz "usuarios" (nao em subcolecao): a busca por uid
 * acontece ANTES de sabermos o tenant — e dela que o oficinaId sai.
 *
 * O documento e chaveado pelo uid do Firebase quando existe (fallback: id do dominio).
 * Isso torna a resolucao do tenant um get direto (roda em toda requisicao) e permite
 * que as security rules encontrem o usuario por caminho: /usuarios/{request.auth.uid}.
 */
@Repository
@Profile("!dev")
public class UsuarioRepositoryFirestore implements UsuarioRepository {

    private final Firestore db;
    private final ConversorFirestore conversor;

    public UsuarioRepositoryFirestore(Firestore db, ConversorFirestore conversor) {
        this.db = db;
        this.conversor = conversor;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        if (usuario.getId() == null || usuario.getId().isBlank()) {
            usuario.setId(UUID.randomUUID().toString());
        }
        String docId = usuario.getUid() != null && !usuario.getUid().isBlank()
                ? usuario.getUid()
                : usuario.getId();
        FirestoreStore.esperar(db.collection("usuarios").document(docId)
                .set(conversor.paraMapa(usuario)));

        // Convidado por e-mail nasce chaveado pelo id; ao ganhar uid muda de documento
        // e o antigo viraria uma duplicata com o mesmo e-mail. Delete e no-op se nao existir.
        if (!docId.equals(usuario.getId())) {
            FirestoreStore.esperar(db.collection("usuarios").document(usuario.getId()).delete());
        }
        return usuario;
    }

    @Override
    public Optional<Usuario> buscarPorId(String id) {
        // O id do dominio nao e a chave do documento — resolve por igualdade.
        return primeiroPor("id", id);
    }

    @Override
    public Optional<Usuario> buscarPorUid(String uid) {
        DocumentSnapshot doc = FirestoreStore.esperar(db.collection("usuarios").document(uid).get());
        if (doc.exists() && doc.getData() != null) {
            return Optional.of(conversor.paraEntidade(doc.getData(), Usuario.class));
        }
        // Usuario convidado por e-mail antes do primeiro login fica chaveado pelo id.
        return primeiroPor("uid", uid);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return primeiroPor("email", email);
    }

    @Override
    public List<Usuario> listarPorOficina(String oficinaId) {
        return FirestoreStore.esperar(db.collection("usuarios")
                        .whereEqualTo("oficinaId", oficinaId).get())
                .getDocuments().stream()
                .map(d -> conversor.paraEntidade(d.getData(), Usuario.class))
                .toList();
    }

    /** Recebe o id do dominio; o documento pode estar chaveado pelo uid. */
    @Override
    public void remover(String id) {
        FirestoreStore.esperar(db.collection("usuarios").whereEqualTo("id", id).limit(1).get())
                .getDocuments().stream()
                .findFirst()
                .ifPresent(doc -> FirestoreStore.esperar(doc.getReference().delete()));
    }

    private Optional<Usuario> primeiroPor(String campo, String valor) {
        return FirestoreStore.esperar(db.collection("usuarios")
                        .whereEqualTo(campo, valor).limit(1).get())
                .getDocuments().stream()
                .findFirst()
                .map(d -> conversor.paraEntidade(d.getData(), Usuario.class));
    }
}
