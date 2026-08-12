package com.trackwheel.domain.storage;

/**
 * Porta de armazenamento de arquivos: fotos do checklist de entrada e assinatura do cliente.
 * A implementacao (Firebase Storage / memoria no dev) fica na infra — o dominio so conhece
 * a URL que sobra no fim, que e o que a OS guarda e o PDF imprime.
 */
public interface ArmazenamentoArquivos {

    /**
     * Grava o arquivo dentro do espaco da oficina e devolve onde ele ficou.
     *
     * @param pasta        agrupador dentro da oficina, ex.: "ordens/{osId}"
     * @param nomeOriginal nome vindo do celular; usado so para preservar a extensao
     */
    ArquivoSalvo salvar(String oficinaId, String pasta, String nomeOriginal,
                        byte[] conteudo, String contentType);

    /**
     * @param caminho onde o objeto vive (util para apagar ou reassinar depois)
     * @param url     endereco para exibir a imagem; e este valor que fica salvo na OS
     */
    record ArquivoSalvo(String caminho, String url) {
    }
}
