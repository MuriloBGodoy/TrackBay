package com.trackwheel.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Venda avulsa (PDV), sem OS. Cliente cadastrado ou consumidor final. */
public class VendaBalcao {

    private String id;
    private String oficinaId;
    private String numero;
    /** Nulo quando e consumidor final. */
    private String clienteId;
    private String clienteNome;
    private List<ItemPeca> itens = new ArrayList<>();
    private BigDecimal descontoGeral = BigDecimal.ZERO;
    private Pagamento pagamento = new Pagamento();
    private String vendedorId;
    private String vendedorNome;
    private Instant dataVenda = Instant.now();
    private boolean cancelada;

    public BigDecimal getSubtotal() {
        return itens.stream()
                .map(ItemPeca::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotal() {
        BigDecimal d = descontoGeral == null ? BigDecimal.ZERO : descontoGeral;
        return getSubtotal().subtract(d).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isConsumidorFinal() {
        return clienteId == null;
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

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public void setClienteNome(String clienteNome) {
        this.clienteNome = clienteNome;
    }

    public List<ItemPeca> getItens() {
        return itens;
    }

    public void setItens(List<ItemPeca> itens) {
        this.itens = itens == null ? new ArrayList<>() : new ArrayList<>(itens);
    }

    public BigDecimal getDescontoGeral() {
        return descontoGeral;
    }

    public void setDescontoGeral(BigDecimal descontoGeral) {
        this.descontoGeral = descontoGeral;
    }

    public Pagamento getPagamento() {
        return pagamento;
    }

    public void setPagamento(Pagamento pagamento) {
        this.pagamento = pagamento == null ? new Pagamento() : pagamento;
    }

    public String getVendedorId() {
        return vendedorId;
    }

    public void setVendedorId(String vendedorId) {
        this.vendedorId = vendedorId;
    }

    public String getVendedorNome() {
        return vendedorNome;
    }

    public void setVendedorNome(String vendedorNome) {
        this.vendedorNome = vendedorNome;
    }

    public Instant getDataVenda() {
        return dataVenda;
    }

    public void setDataVenda(Instant dataVenda) {
        this.dataVenda = dataVenda;
    }

    public boolean isCancelada() {
        return cancelada;
    }

    public void setCancelada(boolean cancelada) {
        this.cancelada = cancelada;
    }
}
