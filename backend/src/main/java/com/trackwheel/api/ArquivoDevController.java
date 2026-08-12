package com.trackwheel.api;

import com.trackwheel.domain.service.RecursoNaoEncontradoException;
import com.trackwheel.infrastructure.storage.ArmazenamentoMemoriaDev;
import com.trackwheel.security.ContextoTenant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serve as imagens guardadas em memoria no perfil dev — o equivalente local da URL
 * tokenizada que o Firebase Storage devolve em producao. Nao existe fora do dev.
 */
@RestController
@RequestMapping("/api/arquivos/dev")
@Profile("dev")
@Tag(name = "Arquivos")
public class ArquivoDevController {

    private final ArmazenamentoMemoriaDev armazenamento;

    public ArquivoDevController(ArmazenamentoMemoriaDev armazenamento) {
        this.armazenamento = armazenamento;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Baixa uma imagem enviada no modo dev")
    public ResponseEntity<byte[]> baixar(@PathVariable String id) {
        ArmazenamentoMemoriaDev.Arquivo arquivo = armazenamento.buscar(id)
                // Uma oficina nunca ve o arquivo de outra, nem com o id em maos.
                .filter(a -> a.oficinaId().equals(ContextoTenant.oficinaId()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Arquivo", id));

        MediaType tipo = arquivo.contentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(arquivo.contentType());
        return ResponseEntity.ok().contentType(tipo).body(arquivo.conteudo());
    }
}
