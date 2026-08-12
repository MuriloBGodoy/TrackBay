package com.trackwheel.api;

import com.trackwheel.domain.model.Agendamento;
import com.trackwheel.domain.service.AgendamentoService;
import com.trackwheel.domain.service.RecursoNaoEncontradoException;
import com.trackwheel.security.ContextoTenant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/agenda")
@Tag(name = "Agenda", description = "Agendamento de servicos")
public class AgendamentoController {

    private final AgendamentoService service;

    public AgendamentoController(AgendamentoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Agendamentos do dia ou do periodo",
            description = "Sem parametros, devolve o dia de hoje (visao mobile).")
    public List<Agendamento> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        String oficinaId = ContextoTenant.oficinaId();
        if (de != null && ate != null) {
            return service.listarPorPeriodo(oficinaId, de, ate);
        }
        return service.listarPorDia(oficinaId, dia == null ? LocalDate.now() : dia);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um agendamento por id")
    public Agendamento porId(@PathVariable String id) {
        return service.buscarPorId(ContextoTenant.oficinaId(), id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento", id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um agendamento",
            description = "Rejeita conflito de horario do mesmo mecanico.")
    public Agendamento criar(@RequestBody Agendamento agendamento) {
        agendamento.setId(null);
        return service.salvar(ContextoTenant.oficinaId(), agendamento);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um agendamento")
    public Agendamento atualizar(@PathVariable String id, @RequestBody Agendamento agendamento) {
        agendamento.setId(id);
        return service.salvar(ContextoTenant.oficinaId(), agendamento);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um agendamento")
    public ResponseEntity<Void> remover(@PathVariable String id) {
        service.remover(ContextoTenant.oficinaId(), id);
        return ResponseEntity.noContent().build();
    }
}
