package com.trackwheel.api;

import com.trackwheel.domain.model.VendaBalcao;
import com.trackwheel.domain.service.RecursoNaoEncontradoException;
import com.trackwheel.domain.service.VendaBalcaoService;
import com.trackwheel.security.ContextoTenant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vendas")
@Tag(name = "Venda de balcao", description = "PDV simples, sem OS")
public class VendaBalcaoController {

    private final VendaBalcaoService service;

    public VendaBalcaoController(VendaBalcaoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista as vendas de balcao")
    public List<VendaBalcao> listar() {
        return service.listar(ContextoTenant.oficinaId());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma venda por id")
    public VendaBalcao porId(@PathVariable String id) {
        return service.buscarPorId(ContextoTenant.oficinaId(), id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Venda", id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra uma venda",
            description = "Baixa o estoque na hora. Sem clienteId, a venda e para consumidor final.")
    public VendaBalcao registrar(@RequestBody VendaBalcao venda) {
        venda.setId(null);
        return service.registrar(ContextoTenant.oficinaId(), venda, ContextoTenant.usuario());
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancela a venda", description = "Devolve os itens ao estoque.")
    public VendaBalcao cancelar(@PathVariable String id) {
        return service.cancelar(ContextoTenant.oficinaId(), id, ContextoTenant.usuario());
    }
}
