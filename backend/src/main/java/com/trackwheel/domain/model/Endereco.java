package com.trackwheel.domain.model;

/** Value object de endereco. Imutavel. */
public record Endereco(
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf
) {
    public static Endereco vazio() {
        return new Endereco(null, null, null, null, null, null, null);
    }

    public String resumo() {
        StringBuilder sb = new StringBuilder();
        if (logradouro != null) {
            sb.append(logradouro);
        }
        if (numero != null) {
            sb.append(", ").append(numero);
        }
        if (bairro != null) {
            sb.append(" - ").append(bairro);
        }
        if (cidade != null) {
            sb.append(", ").append(cidade);
        }
        if (uf != null) {
            sb.append("/").append(uf);
        }
        return sb.toString();
    }
}
