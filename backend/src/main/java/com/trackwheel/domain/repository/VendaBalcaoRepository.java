package com.trackwheel.domain.repository;

import com.trackwheel.domain.model.VendaBalcao;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface VendaBalcaoRepository {

    VendaBalcao salvar(VendaBalcao venda);

    Optional<VendaBalcao> buscarPorId(String oficinaId, String id);

    List<VendaBalcao> listarPorOficina(String oficinaId);

    List<VendaBalcao> listarPorPeriodo(String oficinaId, Instant de, Instant ate);
}
