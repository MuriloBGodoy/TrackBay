package com.trackwheel.api;

import com.trackwheel.domain.model.Oficina;
import com.trackwheel.domain.model.Usuario;
import com.trackwheel.domain.service.OficinaService;
import com.trackwheel.domain.service.RecursoNaoEncontradoException;
import com.trackwheel.security.AcessoNegadoException;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/oficina")
@Tag(name = "Oficina", description = "Onboarding, configuracao e equipe")
public class OficinaController {

    private final OficinaService service;

    public OficinaController(OficinaService service) {
        this.service = service;
    }

    @GetMapping("/me")
    @Operation(summary = "Usuario logado e sua oficina",
            description = "Se oficina vier nula, o usuario ainda precisa fazer o onboarding.")
    public Sessao me() {
        Usuario usuario = ContextoTenant.usuario();
        Oficina oficina = usuario.getOficinaId() == null
                ? null
                : service.buscarPorId(usuario.getOficinaId()).orElse(null);
        return new Sessao(usuario, oficina, oficina == null);
    }

    public record Sessao(Usuario usuario, Oficina oficina, boolean precisaOnboarding) {
    }

    @PostMapping("/onboarding")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria a oficina do usuario logado",
            description = "Promove quem cadastrou a OWNER e ja semeia os campos dinamicos dos ramos escolhidos.")
    public Oficina onboarding(@RequestBody Oficina oficina) {
        oficina.setId(null);
        return service.onboarding(oficina, ContextoTenant.usuario());
    }

    @GetMapping
    @Operation(summary = "Dados da oficina atual")
    public Oficina atual() {
        String id = ContextoTenant.oficinaId();
        return service.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Oficina", id));
    }

    @PutMapping
    @Operation(summary = "Atualiza a oficina",
            description = "Ramo novo entra ja com os campos padrao daquele ramo. So o dono pode alterar.")
    public Oficina atualizar(@RequestBody Oficina oficina) {
        exigirDono();
        return service.atualizar(ContextoTenant.oficinaId(), oficina);
    }

    @GetMapping("/usuarios")
    @Operation(summary = "Equipe da oficina")
    public List<Usuario> usuarios() {
        return service.listarUsuarios(ContextoTenant.oficinaId());
    }

    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adiciona um membro na equipe")
    public Usuario adicionarUsuario(@RequestBody Usuario usuario) {
        exigirDono();
        usuario.setId(null);
        return service.salvarUsuario(ContextoTenant.oficinaId(), usuario);
    }

    @PutMapping("/usuarios/{id}")
    @Operation(summary = "Atualiza papel ou dados de um membro")
    public Usuario atualizarUsuario(@PathVariable String id, @RequestBody Usuario usuario) {
        exigirDono();
        usuario.setId(id);
        return service.salvarUsuario(ContextoTenant.oficinaId(), usuario);
    }

    @DeleteMapping("/usuarios/{id}")
    @Operation(summary = "Remove um membro", description = "A oficina nao pode ficar sem OWNER.")
    public ResponseEntity<Void> removerUsuario(@PathVariable String id) {
        exigirDono();
        service.removerUsuario(ContextoTenant.oficinaId(), id);
        return ResponseEntity.noContent().build();
    }

    private void exigirDono() {
        if (!ContextoTenant.usuario().podeGerenciarOficina()) {
            throw new AcessoNegadoException("Apenas o dono pode gerenciar a oficina e a equipe");
        }
    }
}
