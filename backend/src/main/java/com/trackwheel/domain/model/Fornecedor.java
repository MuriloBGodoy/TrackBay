package com.trackwheel.domain.model;

import java.time.Instant;

public class Fornecedor {

    private String id;
    private String oficinaId;
    private String nome;
    private String cnpj;
    private String contatoNome;
    private String telefone;
    private String email;
    private Endereco endereco = Endereco.vazio();
    private Integer prazoEntregaDias;
    private String condicoes;
    private Instant criadoEm = Instant.now();
    private boolean ativo = true;

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

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getContatoNome() {
        return contatoNome;
    }

    public void setContatoNome(String contatoNome) {
        this.contatoNome = contatoNome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public Integer getPrazoEntregaDias() {
        return prazoEntregaDias;
    }

    public void setPrazoEntregaDias(Integer prazoEntregaDias) {
        this.prazoEntregaDias = prazoEntregaDias;
    }

    public String getCondicoes() {
        return condicoes;
    }

    public void setCondicoes(String condicoes) {
        this.condicoes = condicoes;
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
