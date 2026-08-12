package com.trackwheel.domain.model;

import java.time.Instant;

/**
 * Usuario do sistema, sempre vinculado a uma oficina.
 * O uid vem do Firebase Auth — e a chave que liga o token ao tenant.
 */
public class Usuario {

    private String id;
    private String uid;
    private String oficinaId;
    private String nome;
    private String email;
    private String fotoUrl;
    private Papel papel = Papel.ATTENDANT;
    private boolean ativo = true;
    private Instant criadoEm = Instant.now();
    private Instant ultimoAcesso;

    public Usuario() {
    }

    public Usuario(String id, String uid, String oficinaId, String nome, String email, Papel papel) {
        this.id = id;
        this.uid = uid;
        this.oficinaId = oficinaId;
        this.nome = nome;
        this.email = email;
        this.papel = papel;
    }

    public boolean podeGerenciarOficina() {
        return papel == Papel.OWNER;
    }

    public boolean podeGerenciarEstoque() {
        return papel == Papel.OWNER || papel == Papel.MANAGER;
    }

    public boolean podeCriarOS() {
        return papel != Papel.MECHANIC;
    }

    /** Mecanico so enxerga as OS atribuidas a ele. */
    public boolean veApenasOSAtribuidas() {
        return papel == Papel.MECHANIC;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public Papel getPapel() {
        return papel;
    }

    public void setPapel(Papel papel) {
        this.papel = papel;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    public Instant getUltimoAcesso() {
        return ultimoAcesso;
    }

    public void setUltimoAcesso(Instant ultimoAcesso) {
        this.ultimoAcesso = ultimoAcesso;
    }
}
