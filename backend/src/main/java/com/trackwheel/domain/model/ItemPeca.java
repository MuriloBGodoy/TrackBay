package com.trackwheel.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Linha de peca da OS. A origem define se ha baixa de estoque na conclusao. */
public class ItemPeca {

    private String id;
    private String produtoId;
    private String descricao;
    private String codigo;
    private BigDecimal quantidade = BigDecimal.ONE;
    private BigDecimal valorUnitario = BigDecimal.ZERO;
    private BigDecimal desconto = BigDecimal.ZERO;
    private OrigemPeca origem = OrigemPeca.ESTOQUE_PROPRIO;

    public BigDecimal getTotal() {
        BigDecimal bruto = nz(valorUnitario).multiply(nz(quantidade));
        return bruto.subtract(nz(desconto)).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(String produtoId) {
        this.produtoId = produtoId;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public void setDesconto(BigDecimal desconto) {
        this.desconto = desconto;
    }

    public OrigemPeca getOrigem() {
        return origem;
    }

    public void setOrigem(OrigemPeca origem) {
        this.origem = origem;
    }
}
