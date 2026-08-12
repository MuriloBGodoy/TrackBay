package com.trackwheel.domain.repository;

import com.trackwheel.domain.model.Veiculo;

import java.util.List;
import java.util.Optional;

public interface VeiculoRepository {

    Veiculo salvar(Veiculo veiculo);

    Optional<Veiculo> buscarPorId(String oficinaId, String id);

    /** A busca principal do app. Recebe a placa ja normalizada. */
    Optional<Veiculo> buscarPorPlaca(String oficinaId, String placa);

    List<Veiculo> listarPorOficina(String oficinaId);

    List<Veiculo> listarPorCliente(String oficinaId, String clienteId);

    /** Busca parcial por placa, marca ou modelo. */
    List<Veiculo> buscar(String oficinaId, String termo);

    void remover(String oficinaId, String id);
}
