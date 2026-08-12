package com.trackwheel.api;

import com.trackwheel.domain.service.DashboardService;
import com.trackwheel.security.ContextoTenant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Numeros da tela inicial")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Resumo da oficina",
            description = "Faturamento do dia e do mes (OS entregues + vendas de balcao), "
                    + "OS abertas, ticket medio e alertas de estoque.")
    public DashboardService.Resumo resumo() {
        return service.resumo(ContextoTenant.oficinaId());
    }
}
