package com.trackwheel.domain.model;

import com.trackwheel.domain.validation.PlacaValidator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Veiculo de um cliente. A placa e unica por oficina e sempre normalizada. */
public class Veiculo {

    private String id;
    private String oficinaId;
    private String clienteId;
    private String placa;
    private String marca;
    private String modelo;
    private String versao;
    private Integer anoFabricacao;
    private Integer anoModelo;
    private String cor;
    private String chassi;
    private String renavam;
    private Combustivel combustivel;
    private Cambio cambio;
    private String motorizacao;
    private Integer kmAtual;
    private TipoVeiculo tipoVeiculo = TipoVeiculo.CARRO;
    private List<String> fotos = new ArrayList<>();
    private String observacoes;
    private String avariasPreExistentes;
    private List<HistoricoProprietario> historicoProprietarios = new ArrayList<>();
    private Instant criadoEm = Instant.now();
    private boolean ativo = true;

    public record HistoricoProprietario(String clienteId, String nome, Instant de, Instant ate) {
    }

    /** Normaliza sempre que a placa entra no dominio: maiusculo, sem hifen. */
    public void setPlaca(String placa) {
        this.placa = PlacaValidator.normalizar(placa);
    }

    public String getPlaca() {
        return placa;
    }

    public String getPlacaFormatada() {
        return PlacaValidator.formatar(placa);
    }

    /** Troca de dono: fecha o periodo do anterior e passa a apontar para o novo. */
    public void transferirPara(String novoClienteId, String nomeAnterior) {
        if (this.clienteId != null) {
            historicoProprietarios.add(new HistoricoProprietario(this.clienteId, nomeAnterior, this.criadoEm, Instant.now()));
        }
        this.clienteId = novoClienteId;
    }

    /** A cada OS o km entra atualizado; nunca deixar o odometro andar para tras. */
    public void atualizarKm(Integer km) {
        if (km != null && (this.kmAtual == null || km > this.kmAtual)) {
            this.kmAtual = km;
        }
    }

    public String descricaoCurta() {
        StringBuilder sb = new StringBuilder();
        if (marca != null) {
            sb.append(marca);
        }
        if (modelo != null) {
            sb.append(" ").append(modelo);
        }
        if (anoModelo != null) {
            sb.append(" ").append(anoModelo);
        }
        return sb.toString().trim();
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

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getVersao() {
        return versao;
    }

    public void setVersao(String versao) {
        this.versao = versao;
    }

    public Integer getAnoFabricacao() {
        return anoFabricacao;
    }

    public void setAnoFabricacao(Integer anoFabricacao) {
        this.anoFabricacao = anoFabricacao;
    }

    public Integer getAnoModelo() {
        return anoModelo;
    }

    public void setAnoModelo(Integer anoModelo) {
        this.anoModelo = anoModelo;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public String getChassi() {
        return chassi;
    }

    public void setChassi(String chassi) {
        this.chassi = chassi == null ? null : chassi.toUpperCase();
    }

    public String getRenavam() {
        return renavam;
    }

    public void setRenavam(String renavam) {
        this.renavam = renavam;
    }

    public Combustivel getCombustivel() {
        return combustivel;
    }

    public void setCombustivel(Combustivel combustivel) {
        this.combustivel = combustivel;
    }

    public Cambio getCambio() {
        return cambio;
    }

    public void setCambio(Cambio cambio) {
        this.cambio = cambio;
    }

    public String getMotorizacao() {
        return motorizacao;
    }

    public void setMotorizacao(String motorizacao) {
        this.motorizacao = motorizacao;
    }

    public Integer getKmAtual() {
        return kmAtual;
    }

    public void setKmAtual(Integer kmAtual) {
        this.kmAtual = kmAtual;
    }

    public TipoVeiculo getTipoVeiculo() {
        return tipoVeiculo;
    }

    public void setTipoVeiculo(TipoVeiculo tipoVeiculo) {
        this.tipoVeiculo = tipoVeiculo;
    }

    public List<String> getFotos() {
        return fotos;
    }

    public void setFotos(List<String> fotos) {
        this.fotos = fotos == null ? new ArrayList<>() : new ArrayList<>(fotos);
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public String getAvariasPreExistentes() {
        return avariasPreExistentes;
    }

    public void setAvariasPreExistentes(String avariasPreExistentes) {
        this.avariasPreExistentes = avariasPreExistentes;
    }

    public List<HistoricoProprietario> getHistoricoProprietarios() {
        return historicoProprietarios;
    }

    public void setHistoricoProprietarios(List<HistoricoProprietario> historicoProprietarios) {
        this.historicoProprietarios = historicoProprietarios == null ? new ArrayList<>() : new ArrayList<>(historicoProprietarios);
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
