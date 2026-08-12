package com.trackwheel.api;

import com.trackwheel.domain.model.Cliente;
import com.trackwheel.domain.service.ClienteService;
import com.trackwheel.domain.service.RecursoNaoEncontradoException;
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

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Cadastro de clientes PF e PJ")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista ou busca clientes",
            description = "Sem o parametro busca, lista todos. Com busca, procura por nome, documento ou telefone.")
    public List<Cliente> listar(@RequestParam(required = false) String busca) {
        return service.buscar(ContextoTenant.oficinaId(), busca);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um cliente por id")
    public Cliente porId(@PathVariable String id) {
        return service.buscarPorId(ContextoTenant.oficinaId(), id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um cliente",
            description = "Valida CPF para PF e CNPJ para PJ. Documento duplicado na mesma oficina e rejeitado.")
    public Cliente criar(@RequestBody Cliente cliente) {
        cliente.setId(null);
        return service.salvar(ContextoTenant.oficinaId(), cliente);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um cliente")
    public Cliente atualizar(@PathVariable String id, @RequestBody Cliente cliente) {
        String oficinaId = ContextoTenant.oficinaId();
        service.buscarPorId(oficinaId, id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
        cliente.setId(id);
        return service.salvar(oficinaId, cliente);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um cliente (LGPD: exclusao a pedido do titular)")
    public ResponseEntity<Void> remover(@PathVariable String id) {
        service.remover(ContextoTenant.oficinaId(), id);
        return ResponseEntity.noContent().build();
    }
}
