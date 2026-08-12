package com.trackwheel.domain.model;

import java.time.Instant;
import java.time.LocalDateTime;

/** Agendamento de servico. Visao dia/semana no desktop, lista por dia no mobile. */
public class Agendamento {

    private String id;
    private String oficinaId;
    private String clienteId;
    private String clienteNome;
    private String veiculoId;
    private String veiculoPlaca;
    private LocalDateTime inicio;
    private int duracaoEstimadaMinutos = 60;
    private String servicoPrevisto;
    private String observacoes;
    private String mecanicoId;
    private Status status = Status.AGENDADO;
    /** Preenchido quando o agendamento vira OS. */
    private String ordemServicoId;
    private Instant criadoEm = Instant.now();

    public enum Status {
        AGENDADO,
        CONFIRMADO,
        EM_ATENDIMENTO,
        CONCLUIDO,
        NAO_COMPARECEU,
        CANCELADO
    }

    public LocalDateTime getFim() {
        return inicio == null ? null : inicio.plusMinutes(duracaoEstimadaMinutos);
    }

    /** Dois agendamentos conflitam se as janelas se sobrepoem. */
    public boolean conflitaCom(Agendamento outro) {
        if (inicio == null || outro.inicio == null) {
            return false;
        }
        return inicio.isBefore(outro.getFim()) && outro.inicio.isBefore(getFim());
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

    public String getVeiculoId() {
        return veiculoId;
    }

    public void setVeiculoId(String veiculoId) {
        this.veiculoId = veiculoId;
    }

    public String getVeiculoPlaca() {
        return veiculoPlaca;
    }

    public void setVeiculoPlaca(String veiculoPlaca) {
        this.veiculoPlaca = veiculoPlaca;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public int getDuracaoEstimadaMinutos() {
        return duracaoEstimadaMinutos;
    }

    public void setDuracaoEstimadaMinutos(int duracaoEstimadaMinutos) {
        this.duracaoEstimadaMinutos = duracaoEstimadaMinutos;
    }

    public String getServicoPrevisto() {
        return servicoPrevisto;
    }

    public void setServicoPrevisto(String servicoPrevisto) {
        this.servicoPrevisto = servicoPrevisto;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public String getMecanicoId() {
        return mecanicoId;
    }

    public void setMecanicoId(String mecanicoId) {
        this.mecanicoId = mecanicoId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getOrdemServicoId() {
        return ordemServicoId;
    }

    public void setOrdemServicoId(String ordemServicoId) {
        this.ordemServicoId = ordemServicoId;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }
}
