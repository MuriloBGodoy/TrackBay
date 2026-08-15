package com.trackwheel.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Verificacao de disponibilidade")
public class HealthController {

    @Value("${spring.profiles.active:default}")
    private String perfil;

    @GetMapping
    @Operation(summary = "Status da API", description = "Rota publica, nao exige token.")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "app", "Track Bay",
                "perfil", perfil,
                "em", Instant.now().toString()
        );
    }
}
