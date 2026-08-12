package com.trackwheel.infrastructure.memory;

import com.trackwheel.domain.model.Oficina;
import com.trackwheel.domain.repository.OficinaRepository;
import com.trackwheel.domain.validation.DocumentoValidator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Repository
@Profile("dev")
public class OficinaRepositoryMemory implements OficinaRepository {

    private final Map<String, Oficina> dados = new ConcurrentHashMap<>();

    @Override
    public Oficina salvar(Oficina oficina) {
        if (oficina.getId() == null || oficina.getId().isBlank()) {
            oficina.setId(UUID.randomUUID().toString());
        }
        dados.put(oficina.getId(), oficina);
        return oficina;
    }

    @Override
    public Optional<Oficina> buscarPorId(String id) {
        return Optional.ofNullable(dados.get(id));
    }

    @Override
    public Optional<Oficina> buscarPorCnpj(String cnpj) {
        String alvo = DocumentoValidator.somenteDigitos(cnpj);
        return dados.values().stream()
                .filter(o -> alvo.equals(DocumentoValidator.somenteDigitos(o.getCnpj())))
                .findFirst();
    }

    /** Synchronized para nao entregar o mesmo numero de OS a dois atendentes simultaneos. */
    @Override
    public synchronized int proximoNumeroOS(String oficinaId) {
        Oficina oficina = dados.get(oficinaId);
        if (oficina == null) {
            throw new IllegalArgumentException("Oficina nao encontrada: " + oficinaId);
        }
        int numero = oficina.getConfig().getProximoNumeroOS();
        oficina.getConfig().setProximoNumeroOS(numero + 1);
        return numero;
    }
}
