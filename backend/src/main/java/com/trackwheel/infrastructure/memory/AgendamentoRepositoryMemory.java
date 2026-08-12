package com.trackwheel.infrastructure.memory;

import com.trackwheel.domain.model.Agendamento;
import com.trackwheel.domain.repository.AgendamentoRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("dev")
public class AgendamentoRepositoryMemory extends MemoryStore<Agendamento> implements AgendamentoRepository {

    @Override
    protected String id(Agendamento a) {
        return a.getId();
    }

    @Override
    protected void atribuirId(Agendamento a, String id) {
        a.setId(id);
    }

    @Override
    protected String oficinaId(Agendamento a) {
        return a.getOficinaId();
    }

    @Override
    public Agendamento salvar(Agendamento agendamento) {
        return persistir(agendamento);
    }

    @Override
    public Optional<Agendamento> buscarPorId(String oficinaId, String id) {
        return porId(oficinaId, id);
    }

    @Override
    public List<Agendamento> listarPorDia(String oficinaId, LocalDate dia) {
        return filtrar(oficinaId, a -> a.getInicio() != null && a.getInicio().toLocalDate().equals(dia))
                .stream()
                .sorted(Comparator.comparing(Agendamento::getInicio))
                .toList();
    }

    @Override
    public List<Agendamento> listarPorPeriodo(String oficinaId, LocalDate de, LocalDate ate) {
        return filtrar(oficinaId, a -> {
            if (a.getInicio() == null) {
                return false;
            }
            LocalDate d = a.getInicio().toLocalDate();
            return !d.isBefore(de) && !d.isAfter(ate);
        }).stream().sorted(Comparator.comparing(Agendamento::getInicio)).toList();
    }

    @Override
    public void remover(String oficinaId, String id) {
        excluir(oficinaId, id);
    }
}
