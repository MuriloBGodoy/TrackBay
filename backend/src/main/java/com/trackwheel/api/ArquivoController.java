package com.trackwheel.api;

import com.trackwheel.domain.service.RegraNegocioException;
import com.trackwheel.domain.storage.ArmazenamentoArquivos;
import com.trackwheel.security.ContextoTenant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

/**
 * Upload das fotos do checklist e da assinatura do cliente.
 * A oficina destino vem sempre do token — o cliente escolhe no maximo a subpasta.
 */
@RestController
@RequestMapping("/api/arquivos")
@Tag(name = "Arquivos", description = "Fotos do checklist de entrada e assinatura do cliente")
public class ArquivoController {

    private static final Set<String> TIPOS_ACEITOS =
            Set.of("image/jpeg", "image/png", "image/webp", "image/heic");

    private final ArmazenamentoArquivos armazenamento;

    public ArquivoController(ArmazenamentoArquivos armazenamento) {
        this.armazenamento = armazenamento;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Envia uma imagem",
            description = "Devolve a URL para exibir a imagem e guardar no campo da OS. "
                    + "A pasta agrupa os arquivos dentro da oficina, ex.: ordens/{osId}.")
    public ArmazenamentoArquivos.ArquivoSalvo enviar(
            @RequestPart("arquivo") MultipartFile arquivo,
            @RequestParam(defaultValue = "diversos") String pasta) throws IOException {

        if (arquivo.isEmpty()) {
            throw new RegraNegocioException("Arquivo vazio");
        }
        String tipo = arquivo.getContentType();
        if (tipo == null || !TIPOS_ACEITOS.contains(tipo.toLowerCase(Locale.ROOT))) {
            throw new RegraNegocioException("Formato nao aceito: envie uma imagem JPEG, PNG, WEBP ou HEIC");
        }

        return armazenamento.salvar(ContextoTenant.oficinaId(), pastaSegura(pasta),
                arquivo.getOriginalFilename(), arquivo.getBytes(), tipo);
    }

    /**
     * A pasta vem do cliente e vira caminho no bucket: tudo que nao for letra, numero,
     * barra, hifen ou underscore cai fora — inclusive o ponto, o que ja mata o "..".
     */
    private String pastaSegura(String pasta) {
        if (pasta == null) {
            return "diversos";
        }
        String limpa = pasta.trim().replaceAll("[^A-Za-z0-9/_-]", "");
        while (limpa.startsWith("/")) {
            limpa = limpa.substring(1);
        }
        while (limpa.endsWith("/")) {
            limpa = limpa.substring(0, limpa.length() - 1);
        }
        return limpa.isBlank() ? "diversos" : limpa;
    }
}
