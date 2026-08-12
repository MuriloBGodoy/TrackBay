package com.trackwheel.domain.repository;

import com.trackwheel.domain.model.Oficina;

import java.util.Optional;

/** Porta de persistencia da Oficina. A implementacao (Firestore/memoria) fica na infra. */
public interface OficinaRepository {

    Oficina salvar(Oficina oficina);

    Optional<Oficina> buscarPorId(String id);

    Optional<Oficina> buscarPorCnpj(String cnpj);

    /** Reserva e incrementa o proximo numero de OS da oficina. */
    int proximoNumeroOS(String oficinaId);
}
