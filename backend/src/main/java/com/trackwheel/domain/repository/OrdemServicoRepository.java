package com.trackwheel.domain.repository;

import com.trackwheel.domain.model.OrdemServico;
import com.trackwheel.domain.model.StatusOS;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrdemServicoRepository {

    OrdemServico salvar(OrdemServico os);

    Optional<OrdemServico> buscarPorId(String oficinaId, String id);

    Optional<OrdemServico> buscarPorNumero(String oficinaId, String numero);

    List<OrdemServico> listarPorOficina(String oficinaId);

    List<OrdemServico> listarPorStatus(String oficinaId, StatusOS status);

    /** Historico completo do veiculo: toda OS daquela placa. */
    List<OrdemServico> listarPorVeiculo(String oficinaId, String veiculoId);

    List<OrdemServico> listarPorCliente(String oficinaId, String clienteId);

    /** OS atribuidas a um mecanico — e o unico recorte que o papel MECHANIC enxerga. */
    List<OrdemServico> listarPorMecanico(String oficinaId, String mecanicoId);

    List<OrdemServico> listarPorPeriodo(String oficinaId, Instant de, Instant ate);

    void remover(String oficinaId, String id);
}
