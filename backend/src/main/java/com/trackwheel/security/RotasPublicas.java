package com.trackwheel.security;

import java.util.List;

/** Rotas que dispensam token: documentacao, health e o proprio onboarding. */
public final class RotasPublicas {

    private static final List<String> PREFIXOS = List.of(
            "/docs",
            "/v3/api-docs",
            "/swagger-ui",
            "/actuator/health",
            "/api/health"
    );

    private RotasPublicas() {
    }

    public static boolean isPublica(String path) {
        return PREFIXOS.stream().anyMatch(path::startsWith);
    }
}
