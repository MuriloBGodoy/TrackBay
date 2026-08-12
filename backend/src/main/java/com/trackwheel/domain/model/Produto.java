package com.trackwheel.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Peca ou produto vendido no balcao e consumido em OS. */
public class Produto {

    private String id;
    private String oficinaId;
    private String codigoInterno;
    private String codigoBarras;
    private String nome;
    private String descricao;
    private String marca;
    private String categoria;
    private String unidade = "UN";
    private BigDecimal precoCusto = BigDecimal.ZERO;
    private BigDecimal precoVenda = BigDecimal.ZERO;
    private BigDecimal estoqueAtual = BigDecimal.ZERO;
    private BigDecimal estoqueMinimo = BigDecimal.ZERO;
    private String localizacao;
    private String fornecedorId;
    /** Modelos compativeis, ex.: "Gol G5 2008-2012". */
    private List<String> aplicacao = new ArrayList<>();
    private String fotoUrl;
    private String ncm;
    private Instant criadoEm = Instant.now();
    private boolean ativo = true;

    /** Margem percentual sobre o custo. Zero se o custo nao estiver preenchido. */
    public BigDecimal getMargem() {
        if (precoCusto == null || precoCusto.compareTo(BigDecimal.ZERO) == 0 || precoVenda == null) {
            return BigDecimal.ZERO;
        }
        return precoVenda.subtract(precoCusto)
                .divide(precoCusto, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isEstoqueBaixo() {
        return estoqueAtual != null && estoqueMinimo != null
                && estoqueAtual.compareTo(estoqueMinimo) <= 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOficinaId() {
        return oficinaId;
    }

    public void setOficinaId(String oficinaId) {
        this.oficinaId = oficinaId;
    }

    public String getCodigoInterno() {
        return codigoInterno;
    }

    public void setCodigoInterno(String codigoInterno) {
        this.codigoInterno = codigoInterno;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public BigDecimal getPrecoCusto() {
        return precoCusto;
    }

    public void setPrecoCusto(BigDecimal precoCusto) {
        this.precoCusto = precoCusto;
    }

    public BigDecimal getPrecoVenda() {
        return precoVenda;
    }

    public void setPrecoVenda(BigDecimal precoVenda) {
        this.precoVenda = precoVenda;
    }

    public BigDecimal getEstoqueAtual() {
        return estoqueAtual;
    }

    public void setEstoqueAtual(BigDecimal estoqueAtual) {
        this.estoqueAtual = estoqueAtual;
    }

    public BigDecimal getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setEstoqueMinimo(BigDecimal estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public String getFornecedorId() {
        return fornecedorId;
    }

    public void setFornecedorId(String fornecedorId) {
        this.fornecedorId = fornecedorId;
    }

    public List<String> getAplicacao() {
        return aplicacao;
    }

    public void setAplicacao(List<String> aplicacao) {
        this.aplicacao = aplicacao == null ? new ArrayList<>() : new ArrayList<>(aplicacao);
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public String getNcm() {
        return ncm;
    }

    public void setNcm(String ncm) {
        this.ncm = ncm;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
