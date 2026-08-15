package com.trackwheel.domain.model;

/**
 * O que entrou na oficina para ser atendido.
 *
 * Nem toda OS tem carro: as vezes chega so a peca para manutencao (um radiador,
 * uma bomba, um cabecote), sem o veiculo junto. Nesse caso a OS nao tem placa —
 * tem uma descricao do objeto.
 */
public enum TipoObjeto {
    VEICULO("Veiculo"),
    PECA("Peca avulsa");

    private final String rotulo;

    TipoObjeto(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
