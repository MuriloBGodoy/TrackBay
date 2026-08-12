package com.trackwheel.api;

import com.trackwheel.domain.model.OrdemServico;
import com.trackwheel.domain.model.Veiculo;
import com.trackwheel.domain.service.OrdemServicoService;
import com.trackwheel.domain.service.RecursoNaoEncontradoException;
import com.trackwheel.domain.service.VeiculoService;
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
@RequestMapping("/api/veiculos")
@Tag(name = "Veiculos", description = "Cadastro de veiculos e busca por placa")
public class VeiculoController {

    private final VeiculoService service;
    private final OrdemServicoService osService;

    public VeiculoController(VeiculoService service, OrdemServicoService osService) {
        this.service = service;
        this.osService = osService;
    }

    @GetMapping
    @Operation(summary = "Lista ou busca veiculos",
            description = "Busca por placa (qualquer formatacao), marca ou modelo.")
    public List<Veiculo> listar(@RequestParam(required = false) String busca,
                                @RequestParam(required = false) String clienteId) {
        String oficinaId = ContextoTenant.oficinaId();
        if (clienteId != null && !clienteId.isBlank()) {
            return service.listarPorCliente(oficinaId, clienteId);
        }
        return service.buscar(oficinaId, busca);
    }

    @GetMapping("/placa/{placa}")
    @Operation(summary = "Busca exata por placa",
            description = "A busca principal do app. Aceita ABC1234, abc-1234 ou ABC1D23.")
    public Veiculo porPlaca(@PathVariable String placa) {
        return service.buscarPorPlaca(ContextoTenant.oficinaId(), placa)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veiculo com a placa", placa));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um veiculo por id")
    public Veiculo porId(@PathVariable String id) {
        return service.buscarPorId(ContextoTenant.oficinaId(), id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veiculo", id));
    }

    @GetMapping("/{id}/historico")
    @Operation(summary = "Historico completo do veiculo",
            description = "Todas as OS ja abertas para esta placa, da mais recente para a mais antiga.")
    public List<OrdemServico> historico(@PathVariable String id) {
        return osService.historicoDoVeiculo(ContextoTenant.oficinaId(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um veiculo",
            description = "Placa unica por oficina, validada nos padroes antigo e Mercosul.")
    public Veiculo criar(@RequestBody Veiculo veiculo) {
        veiculo.setId(null);
        return service.salvar(ContextoTenant.oficinaId(), veiculo);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um veiculo")
    public Veiculo atualizar(@PathVariable String id, @RequestBody Veiculo veiculo) {
        String oficinaId = ContextoTenant.oficinaId();
        service.buscarPorId(oficinaId, id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veiculo", id));
        veiculo.setId(id);
        return service.salvar(oficinaId, veiculo);
    }

    @PostMapping("/{id}/transferir")
    @Operation(summary = "Transfere o veiculo para outro cliente",
            description = "Guarda o proprietario anterior no historico.")
    public Veiculo transferir(@PathVariable String id, @RequestParam String novoClienteId) {
        return service.transferir(ContextoTenant.oficinaId(), id, novoClienteId);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um veiculo")
    public ResponseEntity<Void> remover(@PathVariable String id) {
        service.remover(ContextoTenant.oficinaId(), id);
        return ResponseEntity.noContent().build();
    }
}
