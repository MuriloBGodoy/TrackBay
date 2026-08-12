package com.trackwheel.infrastructure.memory;

import com.trackwheel.domain.model.VendaBalcao;
import com.trackwheel.domain.repository.VendaBalcaoRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("dev")
public class VendaBalcaoRepositoryMemory extends MemoryStore<VendaBalcao> implements VendaBalcaoRepository {

    @Override
    protected String id(VendaBalcao v) {
        return v.getId();
    }

    @Override
    protected void atribuirId(VendaBalcao v, String id) {
        v.setId(id);
    }

    @Override
    protected String oficinaId(VendaBalcao v) {
        return v.getOficinaId();
    }

    @Override
    public VendaBalcao salvar(VendaBalcao venda) {
        return persistir(venda);
    }

    @Override
    public Optional<VendaBalcao> buscarPorId(String oficinaId, String id) {
        return porId(oficinaId, id);
    }

    @Override
    public List<VendaBalcao> listarPorOficina(String oficinaId) {
        return doTenant(oficinaId).stream()
                .sorted(Comparator.comparing(VendaBalcao::getDataVenda).reversed())
                .toList();
    }

    @Override
    public List<VendaBalcao> listarPorPeriodo(String oficinaId, Instant de, Instant ate) {
        return filtrar(oficinaId, v -> v.getDataVenda() != null
                && !v.getDataVenda().isBefore(de)
                && !v.getDataVenda().isAfter(ate));
    }
}
