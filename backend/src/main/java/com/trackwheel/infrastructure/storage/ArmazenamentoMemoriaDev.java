package com.trackwheel.infrastructure.storage;

import com.trackwheel.domain.storage.ArmazenamentoArquivos;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Armazenamento do perfil dev: guarda os bytes em memoria e serve pela propria API
 * (ArquivoDevController). Some no restart, igual aos repositorios in-memory — o que
 * importa e conseguir fotografar, subir e ver a foto sem criar projeto no Firebase.
 */
@Component
@Profile("dev")
public class ArmazenamentoMemoriaDev implements ArmazenamentoArquivos {

    private final Map<String, Arquivo> arquivos = new ConcurrentHashMap<>();

    /** O oficinaId viaja junto para o GET poder barrar leitura cruzada entre tenants. */
    public record Arquivo(String oficinaId, String contentType, byte[] conteudo) {
    }

    @Override
    public ArquivoSalvo salvar(String oficinaId, String pasta, String nomeOriginal,
                               byte[] conteudo, String contentType) {
        String id = UUID.randomUUID().toString();
        arquivos.put(id, new Arquivo(oficinaId, contentType, conteudo));

        String caminho = "oficinas/%s/%s/%s".formatted(oficinaId, pasta,
                NomeArquivo.seguro(nomeOriginal, contentType));
        return new ArquivoSalvo(caminho, "/api/arquivos/dev/" + id);
    }

    public Optional<Arquivo> buscar(String id) {
        return Optional.ofNullable(arquivos.get(id));
    }
}
