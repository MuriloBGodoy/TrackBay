package com.trackwheel.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/** Documento OpenAPI servido em /v3/api-docs e renderizado pelo Scalar em /docs. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI trackWheelOpenAPI() {
        SecurityScheme bearer = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("ID Token do Firebase Auth (Google Sign-In). "
                        + "No perfil dev a autenticacao e simulada: use o header X-Dev-User com o e-mail.");

        return new OpenAPI()
                .info(new Info()
                        .title("Track Wheel API")
                        .version("0.0.1")
                        .description("""
                                SaaS de gestao para oficinas mecanicas.

                                O diferencial: os campos da Ordem de Servico se adaptam ao ramo da oficina,
                                via templates versionados de campos dinamicos.

                                Multi-tenant: toda entidade pertence a uma oficina e o tenant e sempre
                                derivado do token — nunca de dado enviado pelo cliente.
                                """)
                        .contact(new Contact().name("Track Wheel")))
                .servers(List.of(new Server().url("http://localhost:8080").description("Local")))
                .components(new Components().addSecuritySchemes("bearerAuth", bearer))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
