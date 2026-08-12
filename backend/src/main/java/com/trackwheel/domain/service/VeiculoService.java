package com.trackwheel.domain.service;

import com.trackwheel.domain.model.Veiculo;
import com.trackwheel.domain.repository.ClienteRepository;
import com.trackwheel.domain.repository.VeiculoRepository;
import com.trackwheel.domain.validation.ChassiValidator;
import com.trackwheel.domain.validation.PlacaValidator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/** Regras de veiculo. A placa e unica por oficina e sempre normalizada. */
@Service
public class VeiculoService {

    private final VeiculoRepository repository;
    private final ClienteRepository clienteRepository;

    public VeiculoService(VeiculoRepository repository, ClienteRepository clienteRepository) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
    }

    public Veiculo salvar(String oficinaId, Veiculo veiculo) {
        veiculo.setOficinaId(oficinaId);

        if (veiculo.getPlaca() == null || veiculo.getPlaca().isBlank()) {
            throw new RegraNegocioException("Placa e obrigatoria");
        }
        if (!PlacaValidator.valida(veiculo.getPlaca())) {
            throw new RegraNegocioException("Placa invalida: " + veiculo.getPlaca()
                    + ". Use o padrao antigo (ABC1234) ou Mercosul (ABC1D23).");
        }
        if (veiculo.getChassi() != null && !veiculo.getChassi().isBlank()
                && !ChassiValidator.valido(veiculo.getChassi())) {
            throw new RegraNegocioException("Chassi invalido: deve ter 17 caracteres, sem as letras I, O e Q");
        }
        if (veiculo.getClienteId() == null || veiculo.getClienteId().isBlank()) {
            throw new RegraNegocioException("Veiculo precisa de um proprietario");
        }
        clienteRepository.buscarPorId(oficinaId, veiculo.getClienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", veiculo.getClienteId()));

        Optional<Veiculo> mesmaPlaca = repository.buscarPorPlaca(oficinaId, veiculo.getPlaca());
        if (mesmaPlaca.isPresent() && !mesmaPlaca.get().getId().equals(veiculo.getId())) {
            throw new RegraNegocioException("Ja existe um veiculo com a placa "
                    + veiculo.getPlacaFormatada() + " nesta oficina");
        }
        return repository.salvar(veiculo);
    }

    public Optional<Veiculo> buscarPorId(String oficinaId, String id) {
        return repository.buscarPorId(oficinaId, id);
    }

    /** Busca principal do app. */
    public Optional<Veiculo> buscarPorPlaca(String oficinaId, String placa) {
        return repository.buscarPorPlaca(oficinaId, placa);
    }

    public List<Veiculo> listar(String oficinaId) {
        return repository.listarPorOficina(oficinaId);
    }

    public List<Veiculo> listarPorCliente(String oficinaId, String clienteId) {
        return repository.listarPorCliente(oficinaId, clienteId);
    }

    public List<Veiculo> buscar(String oficinaId, String termo) {
        if (termo == null || termo.isBlank()) {
            return listar(oficinaId);
        }
        return repository.buscar(oficinaId, termo);
    }

    /** Troca de dono preservando o historico de proprietarios. */
    public Veiculo transferir(String oficinaId, String veiculoId, String novoClienteId) {
        Veiculo veiculo = repository.buscarPorId(oficinaId, veiculoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veiculo", veiculoId));
        clienteRepository.buscarPorId(oficinaId, novoClienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", novoClienteId));

        String nomeAnterior = clienteRepository.buscarPorId(oficinaId, veiculo.getClienteId())
                .map(c -> c.getNome())
                .orElse(null);
        veiculo.transferirPara(novoClienteId, nomeAnterior);
        return repository.salvar(veiculo);
    }

    public void remover(String oficinaId, String id) {
        repository.remover(oficinaId, id);
    }
}
