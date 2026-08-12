package com.trackwheel.domain.model;

import java.util.List;

/**
 * Definicao de um campo do formulario dinamico de OS.
 * O schema vive no banco e e renderizado pelo front — nada de campo hardcoded.
 */
public class CampoDinamico {

    private String chave;
    private String rotulo;
    private TipoCampo tipo;
    private boolean obrigatorio;
    private List<String> opcoes = List.of();
    private int ordem;
    private String grupo;
    private Condicional condicional;
    private List<TipoVeiculo> aplicavelA = List.of();
    private String placeholder;
    private String unidade;

    public CampoDinamico() {
    }

    public CampoDinamico(String chave, String rotulo, TipoCampo tipo, boolean obrigatorio,
                         List<String> opcoes, int ordem, String grupo) {
        this.chave = chave;
        this.rotulo = rotulo;
        this.tipo = tipo;
        this.obrigatorio = obrigatorio;
        this.opcoes = opcoes == null ? List.of() : opcoes;
        this.ordem = ordem;
        this.grupo = grupo;
    }

    /** Condicao para exibir o campo: so aparece se outro campo tiver determinado valor. */
    public record Condicional(String campo, Object valor) {
    }

    /** Um campo sem restricao de tipo de veiculo vale para todos. */
    public boolean aplicaSeA(TipoVeiculo tipoVeiculo) {
        return aplicavelA == null || aplicavelA.isEmpty() || aplicavelA.contains(tipoVeiculo);
    }

    public CampoDinamico comCondicional(String campo, Object valor) {
        this.condicional = new Condicional(campo, valor);
        return this;
    }

    public CampoDinamico comAplicavelA(TipoVeiculo... tipos) {
        this.aplicavelA = List.of(tipos);
        return this;
    }

    public CampoDinamico comUnidade(String unidade) {
        this.unidade = unidade;
        return this;
    }

    public String getChave() {
        return chave;
    }

    public void setChave(String chave) {
        this.chave = chave;
    }

    public String getRotulo() {
        return rotulo;
    }

    public void setRotulo(String rotulo) {
        this.rotulo = rotulo;
    }

    public TipoCampo getTipo() {
        return tipo;
    }

    public void setTipo(TipoCampo tipo) {
        this.tipo = tipo;
    }

    public boolean isObrigatorio() {
        return obrigatorio;
    }

    public void setObrigatorio(boolean obrigatorio) {
        this.obrigatorio = obrigatorio;
    }

    public List<String> getOpcoes() {
        return opcoes;
    }

    public void setOpcoes(List<String> opcoes) {
        this.opcoes = opcoes == null ? List.of() : opcoes;
    }

    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public Condicional getCondicional() {
        return condicional;
    }

    public void setCondicional(Condicional condicional) {
        this.condicional = condicional;
    }

    public List<TipoVeiculo> getAplicavelA() {
        return aplicavelA;
    }

    public void setAplicavelA(List<TipoVeiculo> aplicavelA) {
        this.aplicavelA = aplicavelA == null ? List.of() : aplicavelA;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }
}
