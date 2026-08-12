package com.trackwheel.infrastructure.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Base dos repositorios in-memory usados no perfil dev.
 * Todo acesso passa pelo oficinaId: o mesmo isolamento de tenant que o Firestore aplica.
 */
abstract class MemoryStore<T> {

    private final Map<String, T> dados = new ConcurrentHashMap<>();

    protected abstract String id(T entidade);

    protected abstract void atribuirId(T entidade, String id);

    protected abstract String oficinaId(T entidade);

    protected T persistir(T entidade) {
        if (id(entidade) == null || id(entidade).isBlank()) {
            atribuirId(entidade, UUID.randomUUID().toString());
        }
        dados.put(id(entidade), entidade);
        return entidade;
    }

    protected Optional<T> porId(String oficinaId, String id) {
        return Optional.ofNullable(dados.get(id))
                .filter(e -> oficinaId.equals(oficinaId(e)));
    }

    protected List<T> doTenant(String oficinaId) {
        return dados.values().stream()
                .filter(e -> oficinaId.equals(oficinaId(e)))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    protected List<T> filtrar(String oficinaId, Predicate<T> filtro) {
        return doTenant(oficinaId).stream().filter(filtro).toList();
    }

    protected Optional<T> primeiro(String oficinaId, Predicate<T> filtro) {
        return doTenant(oficinaId).stream().filter(filtro).findFirst();
    }

    protected void excluir(String oficinaId, String id) {
        porId(oficinaId, id).ifPresent(e -> dados.remove(id));
    }

    protected Map<String, T> raw() {
        return dados;
    }

    /** Busca textual simples: casa se qualquer um dos campos contiver o termo. */
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
}
