package com.trackwheel.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.trackwheel.domain.model.Usuario;
import com.trackwheel.domain.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

/**
 * Valida o ID Token do Firebase e resolve o usuario/tenant.
 * O oficinaId sai sempre do usuario persistido, nunca de dado enviado pelo cliente.
 */
@Component
@Profile("!dev")
public class FirebaseAuthFilter extends OncePerRequestFilter {

    private final UsuarioRepository usuarioRepository;

    public FirebaseAuthFilter(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (RotasPublicas.isPublica(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(header.substring(7));
            Optional<Usuario> usuario = usuarioRepository.buscarPorUid(token.getUid());

            if (usuario.isEmpty()) {
                // Primeiro login: o usuario ainda nao tem oficina, so pode fazer onboarding.
                Usuario novo = new Usuario();
                novo.setUid(token.getUid());
                novo.setNome(token.getName());
                novo.setEmail(token.getEmail());
                novo.setFotoUrl(token.getPicture());
                ContextoTenant.definir(novo);
            } else {
                Usuario u = usuario.get();
                if (!u.isAtivo()) {
                    throw new NaoAutenticadoException("Usuario desativado");
                }
                u.setUltimoAcesso(Instant.now());
                ContextoTenant.definir(u);
            }
            chain.doFilter(request, response);
        } catch (FirebaseAuthException e) {
            throw new NaoAutenticadoException("Token invalido: " + e.getMessage());
        } finally {
            ContextoTenant.limpar();
        }
    }
}
