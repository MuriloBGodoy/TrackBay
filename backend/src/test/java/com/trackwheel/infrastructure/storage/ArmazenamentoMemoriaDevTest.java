package com.trackwheel.infrastructure.storage;

import com.trackwheel.domain.storage.ArmazenamentoArquivos.ArquivoSalvo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArmazenamentoMemoriaDevTest {

    private final ArmazenamentoMemoriaDev armazenamento = new ArmazenamentoMemoriaDev();
    private final byte[] conteudo = "foto".getBytes(StandardCharsets.UTF_8);

    @Test
    @DisplayName("salva dentro da pasta do tenant e devolve a URL que serve o arquivo")
    void salvaEServe() {
        ArquivoSalvo salvo = armazenamento.salvar("of-1", "ordens/os-9", "foto.jpg",
                conteudo, "image/jpeg");

        assertTrue(salvo.caminho().startsWith("oficinas/of-1/ordens/os-9/"));
        assertTrue(salvo.caminho().endsWith(".jpg"));
        assertTrue(salvo.url().startsWith("/api/arquivos/dev/"));

        String id = salvo.url().substring(salvo.url().lastIndexOf('/') + 1);
        var arquivo = armazenamento.buscar(id).orElseThrow();
        assertEquals("of-1", arquivo.oficinaId());
        assertEquals("image/jpeg", arquivo.contentType());
        assertArrayEquals(conteudo, arquivo.conteudo());
    }

    @Test
    @DisplayName("duas fotos com o mesmo nome nao colidem")
    void nomesNaoColidem() {
        ArquivoSalvo a = armazenamento.salvar("of-1", "ordens/os-9", "image.jpg", conteudo, "image/jpeg");
        ArquivoSalvo b = armazenamento.salvar("of-1", "ordens/os-9", "image.jpg", conteudo, "image/jpeg");

        assertFalse(a.caminho().equals(b.caminho()));
        assertFalse(a.url().equals(b.url()));
    }

    @Test
    @DisplayName("nome do cliente nao vira caminho: '..' e barras sao descartados")
    void nomeMaliciosoNaoEscapaDaPasta() {
        ArquivoSalvo salvo = armazenamento.salvar("of-1", "ordens/os-9",
                "../../../of-2/roubo.jpg", conteudo, "image/jpeg");

        assertTrue(salvo.caminho().startsWith("oficinas/of-1/ordens/os-9/"),
                "caminho vazou da pasta do tenant: " + salvo.caminho());
        assertFalse(salvo.caminho().contains(".."));
        assertFalse(salvo.caminho().contains("roubo"));
        assertTrue(salvo.caminho().endsWith(".jpg"), "a extensao original e preservada");
    }

    @Test
    @DisplayName("sem extensao no nome, deduz pelo content type")
    void extensaoPeloContentType() {
        ArquivoSalvo salvo = armazenamento.salvar("of-1", "assinaturas", "assinatura",
                conteudo, "image/png");

        assertTrue(salvo.caminho().endsWith(".png"));
    }
}
