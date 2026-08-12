package com.trackwheel.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Catalogo versionado de campos dinamicos de um ramo, dentro de uma oficina.
 * OS antigas continuam renderizando o schema com que foram criadas (ver OrdemServico.schemaVersion).
 */
public class TemplateCampos {

    private String id;
    private String oficinaId;
    private Ramo ramo;
    private int versao = 1;
    private List<CampoDinamico> campos = new ArrayList<>();
    private Instant criadoEm = Instant.now();
    private String criadoPor;
    private boolean ativo = true;

    public TemplateCampos() {
    }

    public TemplateCampos(String id, String oficinaId, Ramo ramo, int versao, List<CampoDinamico> campos) {
        this.id = id;
        this.oficinaId = oficinaId;
        this.ramo = ramo;
        this.versao = versao;
        this.campos = new ArrayList<>(campos);
    }

    /** Campos ordenados e ja filtrados pelo tipo de veiculo da OS. */
    public List<CampoDinamico> camposPara(TipoVeiculo tipoVeiculo) {
        return campos.stream()
                .filter(c -> c.aplicaSeA(tipoVeiculo))
                .sorted(Comparator.comparingInt(CampoDinamico::getOrdem))
                .toList();
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

    public Ramo getRamo() {
        return ramo;
    }

    public void setRamo(Ramo ramo) {
        this.ramo = ramo;
    }

    public int getVersao() {
        return versao;
    }

    public void setVersao(int versao) {
        this.versao = versao;
    }

    public List<CampoDinamico> getCampos() {
        return campos;
    }

    public void setCampos(List<CampoDinamico> campos) {
        this.campos = campos == null ? new ArrayList<>() : new ArrayList<>(campos);
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    public String getCriadoPor() {
        return criadoPor;
    }

    public void setCriadoPor(String criadoPor) {
        this.criadoPor = criadoPor;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
