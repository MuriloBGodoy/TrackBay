package com.trackwheel.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Tenant do sistema. Toda entidade carrega o oficinaId e toda query e filtrada por ele. */
public class Oficina {

    private String id;
    private String nomeFantasia;
    private String razaoSocial;
    private String cnpj;
    private String inscricaoEstadual;
    private List<Ramo> ramos = new ArrayList<>();
    private Endereco endereco = Endereco.vazio();
    private String telefone;
    private String whatsapp;
    private String email;
    private String logoUrl;
    private String horarioFuncionamento;
    private Config config = new Config();
    private Instant criadoEm = Instant.now();
    private boolean ativo = true;

    /** Preferencias da oficina que afetam OS e PDF. */
    public static class Config {
        private String prefixoNumeroOS;
        private int proximoNumeroOS = 1;
        private BigDecimal aliquotaServico = BigDecimal.ZERO;
        private BigDecimal aliquotaPeca = BigDecimal.ZERO;
        private String textoGarantiaPadrao = "Garantia de 90 dias sobre servicos executados, conforme CDC art. 26.";
        private int garantiaPadraoDias = 90;

        public String getPrefixoNumeroOS() {
            return prefixoNumeroOS;
        }

        public void setPrefixoNumeroOS(String prefixoNumeroOS) {
            this.prefixoNumeroOS = prefixoNumeroOS;
        }

        public int getProximoNumeroOS() {
            return proximoNumeroOS;
        }

        public void setProximoNumeroOS(int proximoNumeroOS) {
            this.proximoNumeroOS = proximoNumeroOS;
        }

        public BigDecimal getAliquotaServico() {
            return aliquotaServico;
        }

        public void setAliquotaServico(BigDecimal aliquotaServico) {
            this.aliquotaServico = aliquotaServico;
        }

        public BigDecimal getAliquotaPeca() {
            return aliquotaPeca;
        }

        public void setAliquotaPeca(BigDecimal aliquotaPeca) {
            this.aliquotaPeca = aliquotaPeca;
        }

        public String getTextoGarantiaPadrao() {
            return textoGarantiaPadrao;
        }

        public void setTextoGarantiaPadrao(String textoGarantiaPadrao) {
            this.textoGarantiaPadrao = textoGarantiaPadrao;
        }

        public int getGarantiaPadraoDias() {
            return garantiaPadraoDias;
        }

        public void setGarantiaPadraoDias(int garantiaPadraoDias) {
            this.garantiaPadraoDias = garantiaPadraoDias;
        }
    }

    public boolean atendeRamo(Ramo ramo) {
        return ramos.contains(ramo);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getInscricaoEstadual() {
        return inscricaoEstadual;
    }

    public void setInscricaoEstadual(String inscricaoEstadual) {
        this.inscricaoEstadual = inscricaoEstadual;
    }

    public List<Ramo> getRamos() {
        return ramos;
    }

    public void setRamos(List<Ramo> ramos) {
        this.ramos = ramos == null ? new ArrayList<>() : new ArrayList<>(ramos);
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getHorarioFuncionamento() {
        return horarioFuncionamento;
    }

    public void setHorarioFuncionamento(String horarioFuncionamento) {
        this.horarioFuncionamento = horarioFuncionamento;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
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
