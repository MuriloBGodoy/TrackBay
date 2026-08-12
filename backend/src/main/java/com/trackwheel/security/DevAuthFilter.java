package com.trackwheel.security;

import com.trackwheel.domain.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Autenticacao de desenvolvimento: permite rodar o app sem credencial do Firebase.
 * Resolve o usuario pelo header X-Dev-User (e-mail) ou cai no usuario semeado.
 * Este filtro NUNCA sobe fora do perfil dev — em producao quem vale e o FirebaseAuthFilter.
 */
@Component
@Profile("dev")
public class DevAuthFilter extends OncePerRequestFilter {

    private final UsuarioRepository usuarioRepository;

    public DevAuthFilter(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (RotasPublicas.isPublica(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }
        try {
            String email = request.getHeader("X-Dev-User");
            if (email != null && !email.isBlank()) {
                usuarioRepository.buscarPorEmail(email).ifPresent(ContextoTenant::definir);
            } else {
                usuarioRepository.buscarPorUid(DevDados.UID_DONO).ifPresent(ContextoTenant::definir);
            }
            chain.doFilter(request, response);
        } finally {
            ContextoTenant.limpar();
        }
    }
}
