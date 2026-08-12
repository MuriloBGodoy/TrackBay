package com.trackwheel.api;

import com.trackwheel.domain.model.MovimentacaoEstoque;
import com.trackwheel.domain.model.Produto;
import com.trackwheel.domain.model.TipoMovimentacao;
import com.trackwheel.domain.service.EstoqueService;
import com.trackwheel.domain.service.RecursoNaoEncontradoException;
import com.trackwheel.security.AcessoNegadoException;
import com.trackwheel.security.ContextoTenant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/estoque")
@Tag(name = "Estoque", description = "Produtos, movimentacoes e alertas")
public class EstoqueController {

    private final EstoqueService service;

    public EstoqueController(EstoqueService service) {
        this.service = service;
    }

    @GetMapping("/produtos")
    @Operation(summary = "Lista ou busca produtos",
            description = "Busca por nome, codigo interno, codigo de barras ou marca.")
    public List<Produto> produtos(@RequestParam(required = false) String busca) {
        return service.buscarProdutos(ContextoTenant.oficinaId(), busca);
    }

    @GetMapping("/produtos/{id}")
    @Operation(summary = "Busca um produto por id")
    public Produto produto(@PathVariable String id) {
        return service.buscarProduto(ContextoTenant.oficinaId(), id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", id));
    }

    @GetMapping("/produtos/barras/{codigo}")
    @Operation(summary = "Busca por codigo de barras", description = "Usado pelo leitor no PDV.")
    public Produto porCodigoBarras(@PathVariable String codigo) {
        return service.buscarPorCodigoBarras(ContextoTenant.oficinaId(), codigo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto com codigo de barras", codigo));
    }

    @PostMapping("/produtos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um produto")
    public Produto criarProduto(@RequestBody Produto produto) {
        exigirGestor();
        produto.setId(null);
        return service.salvarProduto(ContextoTenant.oficinaId(), produto);
    }

    @PutMapping("/produtos/{id}")
    @Operation(summary = "Atualiza um produto",
            description = "O estoque nao muda por aqui: use as movimentacoes.")
    public Produto atualizarProduto(@PathVariable String id, @RequestBody Produto produto) {
        exigirGestor();
        String oficinaId = ContextoTenant.oficinaId();
        Produto atual = service.buscarProduto(oficinaId, id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", id));
        produto.setId(id);
        produto.setEstoqueAtual(atual.getEstoqueAtual());
        return service.salvarProduto(oficinaId, produto);
    }

    @DeleteMapping("/produtos/{id}")
    @Operation(summary = "Remove um produto")
    public ResponseEntity<Void> removerProduto(@PathVariable String id) {
        exigirGestor();
        service.removerProduto(ContextoTenant.oficinaId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/movimentacoes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Movimenta o estoque",
            description = "ENTRADA/SAIDA/DEVOLUCAO somam ou subtraem. AJUSTE define o saldo absoluto.")
    public MovimentacaoEstoque movimentar(@RequestBody Movimentacao corpo) {
        exigirGestor();
        return service.movimentar(ContextoTenant.oficinaId(), corpo.produtoId(), corpo.tipo(),
                corpo.quantidade(), corpo.motivo(), null,
                ContextoTenant.usuario().getId(), ContextoTenant.usuario().getNome());
    }

    public record Movimentacao(String produtoId, TipoMovimentacao tipo, BigDecimal quantidade, String motivo) {
    }

    @GetMapping("/movimentacoes")
    @Operation(summary = "Historico de movimentacoes")
    public List<MovimentacaoEstoque> historico(@RequestParam(required = false) String produtoId,
                                               @RequestParam(defaultValue = "100") int limite) {
        String oficinaId = ContextoTenant.oficinaId();
        return produtoId == null
                ? service.historico(oficinaId, limite)
                : service.historicoDoProduto(oficinaId, produtoId);
    }

    @GetMapping("/alertas")
    @Operation(summary = "Produtos no estoque minimo ou abaixo")
    public List<Produto> alertas() {
        return service.listarEstoqueBaixo(ContextoTenant.oficinaId());
    }

    private void exigirGestor() {
        if (!ContextoTenant.usuario().podeGerenciarEstoque()) {
            throw new AcessoNegadoException("Seu papel nao permite gerenciar o estoque");
        }
    }
}
