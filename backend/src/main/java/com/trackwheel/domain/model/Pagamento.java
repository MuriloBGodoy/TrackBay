package com.trackwheel.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Pagamento da OS. Suporta divisao: varias parcelas de formas diferentes
 * (ex.: R$200 no PIX + R$300 em 2x no credito) somando o total.
 */
public class Pagamento {

    private List<Parcela> parcelas = new ArrayList<>();
    private StatusPagamento status = StatusPagamento.PENDENTE;

    /** Uma forma de pagamento usada na OS. */
    public static class Parcela {
        private String id;
        private FormaPagamento forma;
        private BigDecimal valor = BigDecimal.ZERO;
        private int numeroParcelas = 1;
        private LocalDate vencimento;
        private Instant recebidoEm;
        private BigDecimal valorRecebido = BigDecimal.ZERO;
        private BigDecimal taxaMaquininha;
        private String faturaId;

        public boolean isQuitada() {
            return valorRecebido != null && valor != null && valorRecebido.compareTo(valor) >= 0;
        }

        public boolean isAtrasada() {
            return !isQuitada() && vencimento != null && vencimento.isBefore(LocalDate.now());
        }

        /** Valor liquido apos a taxa da maquininha, quando informada. */
        public BigDecimal getValorLiquido() {
            BigDecimal v = valor == null ? BigDecimal.ZERO : valor;
            if (taxaMaquininha == null) {
                return v;
            }
            return v.subtract(v.multiply(taxaMaquininha).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public FormaPagamento getForma() {
            return forma;
        }

        public void setForma(FormaPagamento forma) {
            this.forma = forma;
        }

        public BigDecimal getValor() {
            return valor;
        }

        public void setValor(BigDecimal valor) {
            this.valor = valor;
        }

        public int getNumeroParcelas() {
            return numeroParcelas;
        }

        public void setNumeroParcelas(int numeroParcelas) {
            this.numeroParcelas = numeroParcelas;
        }

        public LocalDate getVencimento() {
            return vencimento;
        }

        public void setVencimento(LocalDate vencimento) {
            this.vencimento = vencimento;
        }

        public Instant getRecebidoEm() {
            return recebidoEm;
        }

        public void setRecebidoEm(Instant recebidoEm) {
            this.recebidoEm = recebidoEm;
        }

        public BigDecimal getValorRecebido() {
            return valorRecebido;
        }

        public void setValorRecebido(BigDecimal valorRecebido) {
            this.valorRecebido = valorRecebido;
        }

        public BigDecimal getTaxaMaquininha() {
            return taxaMaquininha;
        }

        public void setTaxaMaquininha(BigDecimal taxaMaquininha) {
            this.taxaMaquininha = taxaMaquininha;
        }

        public String getFaturaId() {
            return faturaId;
        }

        public void setFaturaId(String faturaId) {
            this.faturaId = faturaId;
        }
    }

    public BigDecimal getTotalPrevisto() {
        return parcelas.stream()
                .map(p -> p.getValor() == null ? BigDecimal.ZERO : p.getValor())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalRecebido() {
        return parcelas.stream()
                .map(p -> p.getValorRecebido() == null ? BigDecimal.ZERO : p.getValorRecebido())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getSaldo() {
        return getTotalPrevisto().subtract(getTotalRecebido());
    }

    /** Recalcula o status a partir do que ja foi recebido e dos vencimentos. */
    public void recalcularStatus() {
        BigDecimal recebido = getTotalRecebido();
        BigDecimal previsto = getTotalPrevisto();
        if (previsto.compareTo(BigDecimal.ZERO) == 0) {
            this.status = StatusPagamento.PENDENTE;
        } else if (recebido.compareTo(previsto) >= 0) {
            this.status = StatusPagamento.PAGO;
        } else if (parcelas.stream().anyMatch(Parcela::isAtrasada)) {
            this.status = StatusPagamento.ATRASADO;
        } else if (recebido.compareTo(BigDecimal.ZERO) > 0) {
            this.status = StatusPagamento.PARCIAL;
        } else {
            this.status = StatusPagamento.PENDENTE;
        }
    }

    public List<Parcela> getParcelas() {
        return parcelas;
    }

    public void setParcelas(List<Parcela> parcelas) {
        this.parcelas = parcelas == null ? new ArrayList<>() : new ArrayList<>(parcelas);
    }

    public StatusPagamento getStatus() {
        return status;
    }

    public void setStatus(StatusPagamento status) {
        this.status = status;
    }
}
