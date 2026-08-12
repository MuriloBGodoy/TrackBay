package com.trackwheel.infrastructure.firestore;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Base dos repositorios Firestore. Cada entidade vive numa subcolecao do tenant
 * (oficinas/{oficinaId}/{colecao}/{id}), entao o isolamento vem do proprio caminho —
 * e as security rules conseguem validar o acesso olhando so o path.
 *
 * Buscas textuais e filtros que o Firestore nao expressa (contains, comparacao entre
 * campos) leem a subcolecao do tenant e filtram em memoria: o volume por oficina e
 * pequeno e isso evita indices compostos no MVP.
 */
abstract class FirestoreStore<T> {

    protected final Firestore db;
    protected final ConversorFirestore conversor;
    private final String nomeColecao;
    private final Class<T> tipo;

    protected FirestoreStore(Firestore db, ConversorFirestore conversor, String nomeColecao, Class<T> tipo) {
        this.db = db;
        this.conversor = conversor;
        this.nomeColecao = nomeColecao;
        this.tipo = tipo;
    }

    protected abstract String id(T entidade);

    protected abstract void atribuirId(T entidade, String id);

    protected abstract String oficinaId(T entidade);

    protected CollectionReference colecao(String oficinaId) {
        return db.collection("oficinas").document(oficinaId).collection(nomeColecao);
    }

    protected T persistir(T entidade) {
        if (id(entidade) == null || id(entidade).isBlank()) {
            atribuirId(entidade, UUID.randomUUID().toString());
        }
        esperar(colecao(oficinaId(entidade)).document(id(entidade)).set(conversor.paraMapa(entidade)));
        return entidade;
    }

    protected Optional<T> porId(String oficinaId, String id) {
        DocumentSnapshot doc = esperar(colecao(oficinaId).document(id).get());
        return converter(doc);
    }

    protected List<T> doTenant(String oficinaId) {
        return converterTodos(esperar(colecao(oficinaId).get()).getDocuments());
    }

    /** Igualdade exata resolvida pelo proprio Firestore (enum vai pelo nome). */
    protected List<T> porCampo(String oficinaId, String campo, Object valor) {
        Object alvo = valor instanceof Enum<?> e ? e.name() : valor;
        return converterTodos(esperar(colecao(oficinaId).whereEqualTo(campo, alvo).get()).getDocuments());
    }

    protected Optional<T> primeiroPorCampo(String oficinaId, String campo, Object valor) {
        return porCampo(oficinaId, campo, valor).stream().findFirst();
    }

    protected List<T> filtrar(String oficinaId, Predicate<T> filtro) {
        return doTenant(oficinaId).stream().filter(filtro).toList();
    }

    protected Optional<T> primeiro(String oficinaId, Predicate<T> filtro) {
        return doTenant(oficinaId).stream().filter(filtro).findFirst();
    }

    protected void excluir(String oficinaId, String id) {
        esperar(colecao(oficinaId).document(id).delete());
    }

    protected Optional<T> converter(DocumentSnapshot doc) {
        if (!doc.exists() || doc.getData() == null) {
            return Optional.empty();
        }
        return Optional.of(conversor.paraEntidade(doc.getData(), tipo));
    }

    protected List<T> converterTodos(List<QueryDocumentSnapshot> docs) {
        return docs.stream()
                .map(d -> conversor.paraEntidade(d.getData(), tipo))
                .toList();
    }

    /** Mesma busca textual do MemoryStore, sobre os dados ja carregados do tenant. */
    @SafeVarargs
    protected final boolean contemTermo(T entidade, String termo, Function<T, String>... campos) {
        if (termo == null || termo.isBlank()) {
            return true;
        }
        String t = termo.toLowerCase().trim();
        for (Function<T, String> campo : campos) {
            String valor = campo.apply(entidade);
            if (valor != null && valor.toLowerCase().contains(t)) {
                return true;
            }
        }
        return false;
    }

    protected static <V> V esperar(ApiFuture<V> futuro) {
        try {
            return futuro.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Operacao no Firestore interrompida", e);
        } catch (java.util.concurrent.ExecutionException e) {
            throw new IllegalStateException("Falha ao acessar o Firestore", e.getCause());
        }
    }
}
