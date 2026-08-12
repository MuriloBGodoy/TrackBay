package com.trackwheel.domain.service;

import com.trackwheel.domain.model.Agendamento;
import com.trackwheel.domain.repository.AgendamentoRepository;
import com.trackwheel.domain.repository.ClienteRepository;
import com.trackwheel.domain.repository.VeiculoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AgendamentoService {

    private final AgendamentoRepository repository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;

    public AgendamentoService(AgendamentoRepository repository,
                              ClienteRepository clienteRepository,
                              VeiculoRepository veiculoRepository) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
    }

    public Agendamento salvar(String oficinaId, Agendamento agendamento) {
        agendamento.setOficinaId(oficinaId);

        if (agendamento.getInicio() == null) {
            throw new RegraNegocioException("Data e hora sao obrigatorias");
        }
        if (agendamento.getDuracaoEstimadaMinutos() <= 0) {
            throw new RegraNegocioException("Duracao estimada invalida");
        }
        if (agendamento.getClienteId() != null) {
            clienteRepository.buscarPorId(oficinaId, agendamento.getClienteId())
                    .ifPresent(c -> agendamento.setClienteNome(c.getNome()));
        }
        if (agendamento.getVeiculoId() != null) {
            veiculoRepository.buscarPorId(oficinaId, agendamento.getVeiculoId())
                    .ifPresent(v -> agendamento.setVeiculoPlaca(v.getPlaca()));
        }

        // Conflito do mesmo mecanico no mesmo horario e erro de agenda, nao de negocio do cliente.
        if (agendamento.getMecanicoId() != null) {
            boolean conflito = repository.listarPorDia(oficinaId, agendamento.getInicio().toLocalDate()).stream()
                    .filter(a -> !a.getId().equals(agendamento.getId()))
                    .filter(a -> agendamento.getMecanicoId().equals(a.getMecanicoId()))
                    .anyMatch(a -> a.conflitaCom(agendamento));
            if (conflito) {
                throw new RegraNegocioException("O mecanico ja tem agendamento neste horario");
            }
        }
        return repository.salvar(agendamento);
    }

    public Optional<Agendamento> buscarPorId(String oficinaId, String id) {
        return repository.buscarPorId(oficinaId, id);
    }

    public List<Agendamento> listarPorDia(String oficinaId, LocalDate dia) {
        return repository.listarPorDia(oficinaId, dia);
    }

    public List<Agendamento> listarPorPeriodo(String oficinaId, LocalDate de, LocalDate ate) {
        return repository.listarPorPeriodo(oficinaId, de, ate);
    }

    public void remover(String oficinaId, String id) {
        repository.remover(oficinaId, id);
    }
}
