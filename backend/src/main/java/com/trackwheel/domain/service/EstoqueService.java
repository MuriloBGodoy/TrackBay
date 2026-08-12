package com.trackwheel.domain.service;

import com.trackwheel.domain.model.MovimentacaoEstoque;
import com.trackwheel.domain.model.Produto;
import com.trackwheel.domain.model.TipoMovimentacao;
import com.trackwheel.domain.repository.MovimentacaoEstoqueRepository;
import com.trackwheel.domain.repository.ProdutoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** Movimentacao de estoque com historico auditavel: todo saldo muda por aqui. */
@Service
public class EstoqueService {

    private final ProdutoRepository produtoRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;

    public EstoqueService(ProdutoRepository produtoRepository,
                          MovimentacaoEstoqueRepository movimentacaoRepository) {
        this.produtoRepository = produtoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    public Produto salvarProduto(String oficinaId, Produto produto) {
        produto.setOficinaId(oficinaId);
        if (produto.getNome() == null || produto.getNome().isBlank()) {
            throw new RegraNegocioException("Nome do produto e obrigatorio");
        }
        if (produto.getPrecoVenda() == null || produto.getPrecoVenda().compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("Preco de venda invalido");
        }
        if (produto.getCodigoBarras() != null && !produto.getCodigoBarras().isBlank()) {
            Optional<Produto> existente = produtoRepository
                    .buscarPorCodigoBarras(oficinaId, produto.getCodigoBarras());
            if (existente.isPresent() && !existente.get().getId().equals(produto.getId())) {
                throw new RegraNegocioException("Ja existe produto com o codigo de barras "
                        + produto.getCodigoBarras());
            }
        }
        return produtoRepository.salvar(produto);
    }

    /**
     * Aplica uma movimentacao e grava o historico. Para AJUSTE, a quantidade e o novo saldo absoluto.
     *
     * @throws RegraNegocioException se a saida deixar o estoque negativo
     */
    public MovimentacaoEstoque movimentar(String oficinaId, String produtoId, TipoMovimentacao tipo,
                                          BigDecimal quantidade, String motivo, String documentoOrigem,
                                          String autorId, String autorNome) {
        if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("Quantidade invalida");
        }
        Produto produto = produtoRepository.buscarPorId(oficinaId, produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", produtoId));

        BigDecimal saldoAnterior = produto.getEstoqueAtual() == null ? BigDecimal.ZERO : produto.getEstoqueAtual();
        BigDecimal saldoPosterior = tipo == TipoMovimentacao.AJUSTE
                ? quantidade
                : saldoAnterior.add(quantidade.multiply(BigDecimal.valueOf(tipo.sinal())));

        if (saldoPosterior.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("Estoque insuficiente de " + produto.getNome()
                    + ": disponivel " + saldoAnterior + ", solicitado " + quantidade);
        }

        produto.setEstoqueAtual(saldoPosterior);
        produtoRepository.salvar(produto);

        MovimentacaoEstoque mov = new MovimentacaoEstoque();
        mov.setOficinaId(oficinaId);
        mov.setProdutoId(produtoId);
        mov.setProdutoNome(produto.getNome());
        mov.setTipo(tipo);
        mov.setQuantidade(quantidade);
        mov.setSaldoAnterior(saldoAnterior);
        mov.setSaldoPosterior(saldoPosterior);
        mov.setValorUnitario(tipo == TipoMovimentacao.ENTRADA ? produto.getPrecoCusto() : produto.getPrecoVenda());
        mov.setMotivo(motivo);
        mov.setDocumentoOrigem(documentoOrigem);
        mov.setAutorId(autorId);
        mov.setAutorNome(autorNome);
        return movimentacaoRepository.salvar(mov);
    }

    public Optional<Produto> buscarProduto(String oficinaId, String id) {
        return produtoRepository.buscarPorId(oficinaId, id);
    }

    public Optional<Produto> buscarPorCodigoBarras(String oficinaId, String codigo) {
        return produtoRepository.buscarPorCodigoBarras(oficinaId, codigo);
    }

    public List<Produto> listarProdutos(String oficinaId) {
        return produtoRepository.listarPorOficina(oficinaId);
    }

    public List<Produto> buscarProdutos(String oficinaId, String termo) {
        if (termo == null || termo.isBlank()) {
            return listarProdutos(oficinaId);
        }
        return produtoRepository.buscar(oficinaId, termo);
    }

    /** Alimenta o alerta de estoque minimo do dashboard. */
    public List<Produto> listarEstoqueBaixo(String oficinaId) {
        return produtoRepository.listarEstoqueBaixo(oficinaId);
    }

    public List<MovimentacaoEstoque> historicoDoProduto(String oficinaId, String produtoId) {
        return movimentacaoRepository.listarPorProduto(oficinaId, produtoId);
    }

    public List<MovimentacaoEstoque> historico(String oficinaId, int limite) {
        return movimentacaoRepository.listarPorOficina(oficinaId, limite);
    }

    public void removerProduto(String oficinaId, String id) {
        produtoRepository.remover(oficinaId, id);
    }
}
