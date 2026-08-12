package com.trackwheel.api;

import com.trackwheel.domain.model.Usuario;
import com.trackwheel.domain.service.RecursoNaoEncontradoException;
import com.trackwheel.infrastructure.storage.ArmazenamentoMemoriaDev;
import com.trackwheel.security.ContextoTenant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArquivoDevControllerTest {

    private final ArmazenamentoMemoriaDev armazenamento = new ArmazenamentoMemoriaDev();
    private final ArquivoDevController controller = new ArquivoDevController(armazenamento);
    private final byte[] conteudo = "foto".getBytes(StandardCharsets.UTF_8);

    private String salvarNaOficina(String oficinaId) {
        String url = armazenamento.salvar(oficinaId, "ordens/os-9", "foto.jpg",
                conteudo, "image/jpeg").url();
        return url.substring(url.lastIndexOf('/') + 1);
    }

    private void autenticarNa(String oficinaId) {
        Usuario usuario = new Usuario();
        usuario.setId("u-1");
        usuario.setOficinaId(oficinaId);
        ContextoTenant.definir(usuario);
    }

    @AfterEach
    void limpar() {
        ContextoTenant.limpar();
    }

    @Test
    @DisplayName("a oficina baixa a propria foto")
    void baixaDaPropriaOficina() {
        String id = salvarNaOficina("of-1");
        autenticarNa("of-1");

        assertArrayEquals(conteudo, controller.baixar(id).getBody());
    }

    @Test
    @DisplayName("outra oficina nao baixa a foto nem com o id em maos")
    void naoBaixaDeOutraOficina() {
        String id = salvarNaOficina("of-1");
        autenticarNa("of-2");

        assertThrows(RecursoNaoEncontradoException.class, () -> controller.baixar(id));
    }
}
