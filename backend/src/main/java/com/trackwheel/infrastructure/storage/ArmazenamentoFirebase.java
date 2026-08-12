package com.trackwheel.infrastructure.storage;

import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BlobInfo;
import com.google.firebase.cloud.StorageClient;
import com.trackwheel.domain.storage.ArmazenamentoArquivos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * Armazenamento em producao: Firebase Storage via Admin SDK.
 *
 * A URL devolvida e a mesma que o getDownloadURL do SDK web produz — objeto privado
 * (as storage.rules negam tudo) com um token de download no metadado. Ela nao expira,
 * o que e proposital: o link fica salvo na OS e precisa continuar valendo anos depois.
 * Em troca, quem tiver o link ve a imagem. Se as fotos precisarem de acesso revogavel,
 * troque por URL assinada com expiracao e guarde o caminho, nao a URL.
 */
@Component
@Profile("!dev")
public class ArmazenamentoFirebase implements ArmazenamentoArquivos {

    private static final String CHAVE_TOKEN = "firebaseStorageDownloadTokens";

    private final String nomeBucket;

    public ArmazenamentoFirebase(@Value("${trackwheel.firebase.storage-bucket:}") String nomeBucket) {
        this.nomeBucket = nomeBucket;
    }

    @Override
    public ArquivoSalvo salvar(String oficinaId, String pasta, String nomeOriginal,
                               byte[] conteudo, String contentType) {
        if (nomeBucket == null || nomeBucket.isBlank()) {
            throw new IllegalStateException(
                    "Configure trackwheel.firebase.storage-bucket (ex.: seu-projeto.appspot.com) "
                            + "para habilitar o upload de fotos e assinatura");
        }

        String caminho = "oficinas/%s/%s/%s".formatted(oficinaId, pasta,
                NomeArquivo.seguro(nomeOriginal, contentType));
        String token = UUID.randomUUID().toString();

        Bucket bucket = StorageClient.getInstance().bucket(nomeBucket);
        BlobInfo info = BlobInfo.newBuilder(bucket.getName(), caminho)
                .setContentType(contentType)
                .setMetadata(Map.of(CHAVE_TOKEN, token))
                .build();
        bucket.getStorage().create(info, conteudo);

        return new ArquivoSalvo(caminho, urlDeDownload(bucket.getName(), caminho, token));
    }

    private String urlDeDownload(String bucket, String caminho, String token) {
        // O path vai inteiro num unico segmento: as barras precisam virar %2F.
        String caminhoCodificado = URLEncoder.encode(caminho, StandardCharsets.UTF_8)
                .replace("+", "%20");
        return "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media&token=%s"
                .formatted(bucket, caminhoCodificado, token);
    }
}
