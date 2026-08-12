package com.trackwheel.domain.service;

import com.trackwheel.domain.model.ItemPeca;
import com.trackwheel.domain.model.TipoMovimentacao;
import com.trackwheel.domain.model.Usuario;
import com.trackwheel.domain.model.VendaBalcao;
import com.trackwheel.domain.repository.ClienteRepository;
import com.trackwheel.domain.repository.VendaBalcaoRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.Optional;

/** PDV simples: venda avulsa sem OS, com baixa de estoque imediata. */
@Service
public class VendaBalcaoService {

    private final VendaBalcaoRepository repository;
    private final ClienteRepository clienteRepository;
    private final EstoqueService estoqueService;

    public VendaBalcaoService(VendaBalcaoRepository repository,
                              ClienteRepository clienteRepository,
                              EstoqueService estoqueService) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.estoqueService = estoqueService;
    }

    public VendaBalcao registrar(String oficinaId, VendaBalcao venda, Usuario vendedor) {
        venda.setOficinaId(oficinaId);

        if (venda.getItens().isEmpty()) {
            throw new RegraNegocioException("A venda precisa de ao menos um item");
        }

        if (venda.getClienteId() != null) {
            var cliente = clienteRepository.buscarPorId(oficinaId, venda.getClienteId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", venda.getClienteId()));
            venda.setClienteNome(cliente.getNome());
        } else {
            venda.setClienteNome("Consumidor final");
        }

        venda.setVendedorId(vendedor.getId());
        venda.setVendedorNome(vendedor.getNome());
        venda.setDataVenda(Instant.now());
        venda.setNumero("V%d-%d".formatted(Year.now().getValue(), System.currentTimeMillis() % 100000));
        venda.getPagamento().recalcularStatus();

        VendaBalcao salva = repository.salvar(venda);

        // Balcao baixa na hora: o produto sai fisicamente com o cliente.
        for (ItemPeca item : venda.getItens()) {
            if (item.getProdutoId() != null && item.getOrigem() != null && item.getOrigem().movimentaEstoque()) {
                estoqueService.movimentar(oficinaId, item.getProdutoId(), TipoMovimentacao.SAIDA,
                        item.getQuantidade(), "Venda balcao " + salva.getNumero(), salva.getId(),
                        vendedor.getId(), vendedor.getNome());
            }
        }
        return salva;
    }

    public Optional<VendaBalcao> buscarPorId(String oficinaId, String id) {
        return repository.buscarPorId(oficinaId, id);
    }

    public List<VendaBalcao> listar(String oficinaId) {
        return repository.listarPorOficina(oficinaId);
    }

    /** Cancelamento devolve os itens ao estoque. */
    public VendaBalcao cancelar(String oficinaId, String id, Usuario autor) {
        VendaBalcao venda = repository.buscarPorId(oficinaId, id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Venda", id));
        if (venda.isCancelada()) {
            throw new RegraNegocioException("Venda " + venda.getNumero() + " ja esta cancelada");
        }
        venda.setCancelada(true);

        for (ItemPeca item : venda.getItens()) {
            if (item.getProdutoId() != null && item.getOrigem() != null && item.getOrigem().movimentaEstoque()) {
                estoqueService.movimentar(oficinaId, item.getProdutoId(), TipoMovimentacao.DEVOLUCAO,
                        item.getQuantidade(), "Cancelamento da venda " + venda.getNumero(), venda.getId(),
                        autor.getId(), autor.getNome());
            }
        }
        return repository.salvar(venda);
    }
}
