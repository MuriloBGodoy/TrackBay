package com.trackwheel.infrastructure.memory;

import com.trackwheel.domain.model.OrdemServico;
import com.trackwheel.domain.model.StatusOS;
import com.trackwheel.domain.repository.OrdemServicoRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("dev")
public class OrdemServicoRepositoryMemory extends MemoryStore<OrdemServico> implements OrdemServicoRepository {

    @Override
    protected String id(OrdemServico os) {
        return os.getId();
    }

    @Override
    protected void atribuirId(OrdemServico os, String id) {
        os.setId(id);
    }

    @Override
    protected String oficinaId(OrdemServico os) {
        return os.getOficinaId();
    }

    @Override
    public OrdemServico salvar(OrdemServico os) {
        return persistir(os);
    }

    @Override
    public Optional<OrdemServico> buscarPorId(String oficinaId, String id) {
        return porId(oficinaId, id);
    }

    @Override
    public Optional<OrdemServico> buscarPorNumero(String oficinaId, String numero) {
        return primeiro(oficinaId, os -> numero.equals(os.getNumero()));
    }

    /** Mais recentes primeiro — e como o app lista. */
    @Override
    public List<OrdemServico> listarPorOficina(String oficinaId) {
        return doTenant(oficinaId).stream()
                .sorted(Comparator.comparing(OrdemServico::getDataAbertura,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    public List<OrdemServico> listarPorStatus(String oficinaId, StatusOS status) {
        return filtrar(oficinaId, os -> status == os.getStatus());
    }

    @Override
    public List<OrdemServico> listarPorVeiculo(String oficinaId, String veiculoId) {
        return filtrar(oficinaId, os -> veiculoId.equals(os.getVeiculoId())).stream()
                .sorted(Comparator.comparing(OrdemServico::getDataAbertura,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    public List<OrdemServico> listarPorCliente(String oficinaId, String clienteId) {
        return filtrar(oficinaId, os -> clienteId.equals(os.getClienteId()));
    }

    @Override
    public List<OrdemServico> listarPorMecanico(String oficinaId, String mecanicoId) {
        return filtrar(oficinaId, os -> os.getItensServico().stream()
                .anyMatch(i -> mecanicoId.equals(i.getMecanicoId())));
    }

    @Override
    public List<OrdemServico> listarPorPeriodo(String oficinaId, Instant de, Instant ate) {
        return filtrar(oficinaId, os -> os.getDataAbertura() != null
                && !os.getDataAbertura().isBefore(de)
                && !os.getDataAbertura().isAfter(ate));
    }

    @Override
    public void remover(String oficinaId, String id) {
        excluir(oficinaId, id);
    }
}
