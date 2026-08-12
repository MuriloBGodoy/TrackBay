package com.trackwheel.domain.repository;

import com.trackwheel.domain.model.Agendamento;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AgendamentoRepository {

    Agendamento salvar(Agendamento agendamento);

    Optional<Agendamento> buscarPorId(String oficinaId, String id);

    List<Agendamento> listarPorDia(String oficinaId, LocalDate dia);

    List<Agendamento> listarPorPeriodo(String oficinaId, LocalDate de, LocalDate ate);

    void remover(String oficinaId, String id);
}
