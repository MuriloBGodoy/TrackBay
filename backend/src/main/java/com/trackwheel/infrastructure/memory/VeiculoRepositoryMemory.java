package com.trackwheel.infrastructure.memory;

import com.trackwheel.domain.model.Veiculo;
import com.trackwheel.domain.repository.VeiculoRepository;
import com.trackwheel.domain.validation.PlacaValidator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile("dev")
public class VeiculoRepositoryMemory extends MemoryStore<Veiculo> implements VeiculoRepository {

    @Override
    protected String id(Veiculo v) {
        return v.getId();
    }

    @Override
    protected void atribuirId(Veiculo v, String id) {
        v.setId(id);
    }

    @Override
    protected String oficinaId(Veiculo v) {
        return v.getOficinaId();
    }

    @Override
    public Veiculo salvar(Veiculo veiculo) {
        return persistir(veiculo);
    }

    @Override
    public Optional<Veiculo> buscarPorId(String oficinaId, String id) {
        return porId(oficinaId, id);
    }

    @Override
    public Optional<Veiculo> buscarPorPlaca(String oficinaId, String placa) {
        String alvo = PlacaValidator.normalizar(placa);
        return primeiro(oficinaId, v -> alvo.equals(v.getPlaca()));
    }

    @Override
    public List<Veiculo> listarPorOficina(String oficinaId) {
        return doTenant(oficinaId);
    }

    @Override
    public List<Veiculo> listarPorCliente(String oficinaId, String clienteId) {
        return filtrar(oficinaId, v -> clienteId.equals(v.getClienteId()));
    }

    @Override
    public List<Veiculo> buscar(String oficinaId, String termo) {
        // A placa e buscada normalizada para achar "abc-1234" digitado de qualquer jeito.
        String placaTermo = PlacaValidator.normalizar(termo);
        return filtrar(oficinaId, v ->
                (!placaTermo.isBlank() && v.getPlaca() != null && v.getPlaca().contains(placaTermo))
                        || contemTermo(v, termo, Veiculo::getMarca, Veiculo::getModelo, Veiculo::getVersao));
    }

    @Override
    public void remover(String oficinaId, String id) {
        excluir(oficinaId, id);
    }
}
