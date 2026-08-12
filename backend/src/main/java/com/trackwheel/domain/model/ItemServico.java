package com.trackwheel.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Linha de servico da OS. O total ja considera quantidade e desconto. */
public class ItemServico {

    private String id;
    private String descricao;
    private String tipo;
    private BigDecimal valorUnitario = BigDecimal.ZERO;
    private BigDecimal quantidade = BigDecimal.ONE;
    private BigDecimal desconto = BigDecimal.ZERO;
    private String mecanicoId;
    private String mecanicoNome;

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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public void setDesconto(BigDecimal desconto) {
        this.desconto = desconto;
    }

    public String getMecanicoId() {
        return mecanicoId;
    }

    public void setMecanicoId(String mecanicoId) {
        this.mecanicoId = mecanicoId;
    }

    public String getMecanicoNome() {
        return mecanicoNome;
    }

    public void setMecanicoNome(String mecanicoNome) {
        this.mecanicoNome = mecanicoNome;
    }
}
