package com.trackwheel.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Ordem de Servico: coracao do sistema.
 * Guarda o schemaVersion usado na criacao para que OS antigas continuem renderizando
 * exatamente os campos com que foram preenchidas, mesmo se o template mudar depois.
 */
public class OrdemServico {

    private String id;
    private String oficinaId;
    private String numero;
    private String clienteId;
    private String clienteNome;
    private String veiculoId;
    private String veiculoPlaca;
    private String veiculoDescricao;
    private Integer kmEntrada;

    private StatusOS status = StatusOS.ORCAMENTO;
    private List<TransicaoStatus> historicoStatus = new ArrayList<>();

    private Instant dataAbertura = Instant.now();
    private Instant previsaoEntrega;
    private Instant dataConclusao;
    private Instant dataEntrega;

    private String reclamacaoCliente;
    private String diagnosticoTecnico;

    /** Ramo que definiu o formulario desta OS. */
    private Ramo ramo;
    private int schemaVersion = 1;
    private java.util.Map<String, Object> camposDinamicos = new java.util.HashMap<>();

    private List<ItemServico> itensServico = new ArrayList<>();
    private List<ItemPeca> itensPeca = new ArrayList<>();

    private BigDecimal descontoGeral = BigDecimal.ZERO;
    private BigDecimal acrescimo = BigDecimal.ZERO;

    private Pagamento pagamento = new Pagamento();

    private Integer garantiaDias;
    private Integer garantiaKm;
    private String textoGarantia;

    private String assinaturaClienteUrl;
    private Instant assinadaEm;

    private ChecklistEntrada checklistEntrada = new ChecklistEntrada();

    private String criadoPor;
    private String atualizadoPor;
    private Instant atualizadoEm = Instant.now();

    /** Registro auditavel de cada mudanca de status. */
    public record TransicaoStatus(StatusOS de, StatusOS para, String autorId, String autorNome,
                                  Instant em, String observacao) {
    }

    /** Estado do veiculo na entrada, com fotos — protege a oficina em disputa. */
    public static class ChecklistEntrada {
        private String nivelCombustivel;
        private String estadoPneus;
        private String avarias;
        private List<String> itensNoVeiculo = new ArrayList<>();
        private List<String> fotos = new ArrayList<>();

        public String getNivelCombustivel() {
            return nivelCombustivel;
        }

        public void setNivelCombustivel(String nivelCombustivel) {
            this.nivelCombustivel = nivelCombustivel;
        }

        public String getEstadoPneus() {
            return estadoPneus;
        }

        public void setEstadoPneus(String estadoPneus) {
            this.estadoPneus = estadoPneus;
        }

        public String getAvarias() {
            return avarias;
        }

        public void setAvarias(String avarias) {
            this.avarias = avarias;
        }

        public List<String> getItensNoVeiculo() {
            return itensNoVeiculo;
        }

        public void setItensNoVeiculo(List<String> itensNoVeiculo) {
            this.itensNoVeiculo = itensNoVeiculo == null ? new ArrayList<>() : new ArrayList<>(itensNoVeiculo);
        }

        public List<String> getFotos() {
            return fotos;
        }

        public void setFotos(List<String> fotos) {
            this.fotos = fotos == null ? new ArrayList<>() : new ArrayList<>(fotos);
        }
    }

    public BigDecimal getSubtotalServicos() {
        return itensServico.stream()
                .map(ItemServico::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getSubtotalPecas() {
        return itensPeca.stream()
                .map(ItemPeca::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotal() {
        BigDecimal total = getSubtotalServicos()
                .add(getSubtotalPecas())
                .subtract(nz(descontoGeral))
                .add(nz(acrescimo));
        return total.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Aplica uma transicao de status validando o fluxo e registrando o historico.
     *
     * @throws IllegalStateException se a transicao nao for permitida a partir do status atual
     */
    public void transicionarPara(StatusOS destino, String autorId, String autorNome, String observacao) {
        if (destino == null) {
            throw new IllegalArgumentException("Status de destino obrigatorio");
        }
        if (destino == this.status) {
            return;
        }
        if (!this.status.podeIrPara(destino)) {
            throw new IllegalStateException(
                    "Transicao invalida: " + this.status + " -> " + destino);
        }
        historicoStatus.add(new TransicaoStatus(this.status, destino, autorId, autorNome, Instant.now(), observacao));
        this.status = destino;
        this.atualizadoPor = autorId;
        this.atualizadoEm = Instant.now();

        if (destino == StatusOS.PRONTA) {
            this.dataConclusao = Instant.now();
        }
        if (destino == StatusOS.ENTREGUE) {
            this.dataEntrega = Instant.now();
            if (this.dataConclusao == null) {
                this.dataConclusao = Instant.now();
            }
        }
    }

    /** Itens so podem mudar enquanto a OS nao chegou a um status terminal. */
    public boolean permiteEdicaoDeItens() {
        return !status.isFinal();
    }

    /** Pecas de estoque proprio geram baixa quando a OS conclui. */
    public List<ItemPeca> pecasQueBaixamEstoque() {
        return itensPeca.stream().filter(p -> p.getOrigem() != null && p.getOrigem().movimentaEstoque()).toList();
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

    public String getVeiculoDescricao() {
        return veiculoDescricao;
    }

    public void setVeiculoDescricao(String veiculoDescricao) {
        this.veiculoDescricao = veiculoDescricao;
    }

    public Integer getKmEntrada() {
        return kmEntrada;
    }

    public void setKmEntrada(Integer kmEntrada) {
        this.kmEntrada = kmEntrada;
    }

    public StatusOS getStatus() {
        return status;
    }

    public void setStatus(StatusOS status) {
        this.status = status;
    }

    public List<TransicaoStatus> getHistoricoStatus() {
        return historicoStatus;
    }

    public void setHistoricoStatus(List<TransicaoStatus> historicoStatus) {
        this.historicoStatus = historicoStatus == null ? new ArrayList<>() : new ArrayList<>(historicoStatus);
    }

    public Instant getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(Instant dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public Instant getPrevisaoEntrega() {
        return previsaoEntrega;
    }

    public void setPrevisaoEntrega(Instant previsaoEntrega) {
        this.previsaoEntrega = previsaoEntrega;
    }

    public Instant getDataConclusao() {
        return dataConclusao;
    }

    public void setDataConclusao(Instant dataConclusao) {
        this.dataConclusao = dataConclusao;
    }

    public Instant getDataEntrega() {
        return dataEntrega;
    }

    public void setDataEntrega(Instant dataEntrega) {
        this.dataEntrega = dataEntrega;
    }

    public String getReclamacaoCliente() {
        return reclamacaoCliente;
    }

    public void setReclamacaoCliente(String reclamacaoCliente) {
        this.reclamacaoCliente = reclamacaoCliente;
    }

    public String getDiagnosticoTecnico() {
        return diagnosticoTecnico;
    }

    public void setDiagnosticoTecnico(String diagnosticoTecnico) {
        this.diagnosticoTecnico = diagnosticoTecnico;
    }

    public Ramo getRamo() {
        return ramo;
    }

    public void setRamo(Ramo ramo) {
        this.ramo = ramo;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public java.util.Map<String, Object> getCamposDinamicos() {
        return camposDinamicos;
    }

    public void setCamposDinamicos(java.util.Map<String, Object> camposDinamicos) {
        this.camposDinamicos = camposDinamicos == null ? new java.util.HashMap<>() : new java.util.HashMap<>(camposDinamicos);
    }

    public List<ItemServico> getItensServico() {
        return itensServico;
    }

    public void setItensServico(List<ItemServico> itensServico) {
        this.itensServico = itensServico == null ? new ArrayList<>() : new ArrayList<>(itensServico);
    }

    public List<ItemPeca> getItensPeca() {
        return itensPeca;
    }

    public void setItensPeca(List<ItemPeca> itensPeca) {
        this.itensPeca = itensPeca == null ? new ArrayList<>() : new ArrayList<>(itensPeca);
    }

    public BigDecimal getDescontoGeral() {
        return descontoGeral;
    }

    public void setDescontoGeral(BigDecimal descontoGeral) {
        this.descontoGeral = descontoGeral;
    }

    public BigDecimal getAcrescimo() {
        return acrescimo;
    }

    public void setAcrescimo(BigDecimal acrescimo) {
        this.acrescimo = acrescimo;
    }

    public Pagamento getPagamento() {
        return pagamento;
    }

    public void setPagamento(Pagamento pagamento) {
        this.pagamento = pagamento == null ? new Pagamento() : pagamento;
    }

    public Integer getGarantiaDias() {
        return garantiaDias;
    }

    public void setGarantiaDias(Integer garantiaDias) {
        this.garantiaDias = garantiaDias;
    }

    public Integer getGarantiaKm() {
        return garantiaKm;
    }

    public void setGarantiaKm(Integer garantiaKm) {
        this.garantiaKm = garantiaKm;
    }

    public String getTextoGarantia() {
        return textoGarantia;
    }

    public void setTextoGarantia(String textoGarantia) {
        this.textoGarantia = textoGarantia;
    }

    public String getAssinaturaClienteUrl() {
        return assinaturaClienteUrl;
    }

    public void setAssinaturaClienteUrl(String assinaturaClienteUrl) {
        this.assinaturaClienteUrl = assinaturaClienteUrl;
    }

    public Instant getAssinadaEm() {
        return assinadaEm;
    }

    public void setAssinadaEm(Instant assinadaEm) {
        this.assinadaEm = assinadaEm;
    }

    public ChecklistEntrada getChecklistEntrada() {
        return checklistEntrada;
    }

    public void setChecklistEntrada(ChecklistEntrada checklistEntrada) {
        this.checklistEntrada = checklistEntrada == null ? new ChecklistEntrada() : checklistEntrada;
    }

    public String getCriadoPor() {
        return criadoPor;
    }

    public void setCriadoPor(String criadoPor) {
        this.criadoPor = criadoPor;
    }

    public String getAtualizadoPor() {
        return atualizadoPor;
    }

    public void setAtualizadoPor(String atualizadoPor) {
        this.atualizadoPor = atualizadoPor;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(Instant atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}
