package com.trackwheel.infrastructure.firestore;

import com.google.cloud.firestore.Firestore;
import com.trackwheel.domain.model.Agendamento;
import com.trackwheel.domain.repository.AgendamentoRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("!dev")
public class AgendamentoRepositoryFirestore extends FirestoreStore<Agendamento>
        implements AgendamentoRepository {

    public AgendamentoRepositoryFirestore(Firestore db, ConversorFirestore conversor) {
        super(db, conversor, "agendamentos", Agendamento.class);
    }

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
        return listarPorPeriodo(oficinaId, dia, dia);
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
