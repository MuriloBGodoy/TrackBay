package com.trackwheel.infrastructure.storage;

import java.util.Locale;
import java.util.UUID;

/**
 * Monta o nome do objeto a partir do que o celular mandou.
 * O nome original nao e confiavel (vem do cliente): so a extensao e aproveitada, e o
 * nome final e um UUID — assim nao ha colisao entre duas fotos "image.jpg" nem caminho
 * fabricado com ".." tentando escapar da pasta do tenant.
 */
final class NomeArquivo {

    private NomeArquivo() {
    }

    static String seguro(String nomeOriginal, String contentType) {
        String extensao = extensaoDe(nomeOriginal);
        if (extensao.isEmpty()) {
            extensao = extensaoPorTipo(contentType);
        }
        return UUID.randomUUID() + extensao;
    }

    private static String extensaoDe(String nomeOriginal) {
        if (nomeOriginal == null) {
            return "";
        }
        // Ignora qualquer caminho: interessa so o que vem depois do ultimo ponto do nome.
        String nome = nomeOriginal.replace('\\', '/');
        nome = nome.substring(nome.lastIndexOf('/') + 1);
        int ponto = nome.lastIndexOf('.');
        if (ponto < 0 || ponto == nome.length() - 1) {
            return "";
        }
        String extensao = nome.substring(ponto + 1).toLowerCase(Locale.ROOT);
        if (!extensao.matches("[a-z0-9]{1,5}")) {
            return "";
        }
        return "." + extensao;
    }

    private static String extensaoPorTipo(String contentType) {
        if (contentType == null) {
            return "";
        }
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/heic" -> ".heic";
            default -> "";
        };
    }
}
