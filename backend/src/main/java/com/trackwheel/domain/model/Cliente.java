package com.trackwheel.domain.model;

import com.trackwheel.domain.validation.DocumentoValidator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Cliente da oficina, PF ou PJ. O tipoPessoa controla quais blocos sao validos:
 * dadosPF so vale para FISICA, dadosPJ so para JURIDICA.
 */
public class Cliente {

    private String id;
    private String oficinaId;
    private TipoPessoa tipoPessoa = TipoPessoa.FISICA;
    private String nome;
    private String telefone;
    private String whatsapp;
    private String email;
    private Endereco endereco = Endereco.vazio();
    private String observacoes;
    private Instant dataCadastro = Instant.now();
    private boolean ativo = true;

    private DadosPF dadosPF;
    private DadosPJ dadosPJ;

    /** Consentimento LGPD para tratamento dos dados pessoais. */
    private boolean consentimentoLgpd;
    private Instant consentimentoEm;

    public static class DadosPF {
        private String cpf;
        private String rg;
        private LocalDate dataNascimento;
        private String cnhNumero;
        private String cnhCategoria;
        private LocalDate cnhValidade;

        public String getCpf() {
            return cpf;
        }

        public void setCpf(String cpf) {
            this.cpf = cpf;
        }

        public String getRg() {
            return rg;
        }

        public void setRg(String rg) {
            this.rg = rg;
        }

        public LocalDate getDataNascimento() {
            return dataNascimento;
        }

        public void setDataNascimento(LocalDate dataNascimento) {
            this.dataNascimento = dataNascimento;
        }

        public String getCnhNumero() {
            return cnhNumero;
        }

        public void setCnhNumero(String cnhNumero) {
            this.cnhNumero = cnhNumero;
        }

        public String getCnhCategoria() {
            return cnhCategoria;
        }

        public void setCnhCategoria(String cnhCategoria) {
            this.cnhCategoria = cnhCategoria;
        }

        public LocalDate getCnhValidade() {
            return cnhValidade;
        }

        public void setCnhValidade(LocalDate cnhValidade) {
            this.cnhValidade = cnhValidade;
        }
    }

    public static class DadosPJ {
        private String cnpj;
        private String razaoSocial;
        private String inscricaoEstadual;
        private String inscricaoMunicipal;
        private ContatoResponsavel contatoResponsavel;
        private CondicoesComerciais condicoesComerciais = new CondicoesComerciais();

        public record ContatoResponsavel(String nome, String cargo, String telefone) {
        }

        /** Condicoes de frotista: prazo, limite e faturamento consolidado. */
        public static class CondicoesComerciais {
            private int prazoPagamentoDias = 0;
            private BigDecimal limiteCredito = BigDecimal.ZERO;
            private String tabelaPreco;
            private boolean faturamentoMensal;

            public int getPrazoPagamentoDias() {
                return prazoPagamentoDias;
            }

            public void setPrazoPagamentoDias(int prazoPagamentoDias) {
                this.prazoPagamentoDias = prazoPagamentoDias;
            }

            public BigDecimal getLimiteCredito() {
                return limiteCredito;
            }

            public void setLimiteCredito(BigDecimal limiteCredito) {
                this.limiteCredito = limiteCredito;
            }

            public String getTabelaPreco() {
                return tabelaPreco;
            }

            public void setTabelaPreco(String tabelaPreco) {
                this.tabelaPreco = tabelaPreco;
            }

            public boolean isFaturamentoMensal() {
                return faturamentoMensal;
            }

            public void setFaturamentoMensal(boolean faturamentoMensal) {
                this.faturamentoMensal = faturamentoMensal;
            }
        }

        public String getCnpj() {
            return cnpj;
        }

        public void setCnpj(String cnpj) {
            this.cnpj = cnpj;
        }

        public String getRazaoSocial() {
            return razaoSocial;
        }

        public void setRazaoSocial(String razaoSocial) {
            this.razaoSocial = razaoSocial;
        }

        public String getInscricaoEstadual() {
            return inscricaoEstadual;
        }

        public void setInscricaoEstadual(String inscricaoEstadual) {
            this.inscricaoEstadual = inscricaoEstadual;
        }

        public String getInscricaoMunicipal() {
            return inscricaoMunicipal;
        }

        public void setInscricaoMunicipal(String inscricaoMunicipal) {
            this.inscricaoMunicipal = inscricaoMunicipal;
        }

        public ContatoResponsavel getContatoResponsavel() {
            return contatoResponsavel;
        }

        public void setContatoResponsavel(ContatoResponsavel contatoResponsavel) {
            this.contatoResponsavel = contatoResponsavel;
        }

        public CondicoesComerciais getCondicoesComerciais() {
            return condicoesComerciais;
        }

        public void setCondicoesComerciais(CondicoesComerciais condicoesComerciais) {
            this.condicoesComerciais = condicoesComerciais;
        }
    }

    /** Documento principal conforme o tipo, so digitos. */
    public String documento() {
        if (tipoPessoa == TipoPessoa.FISICA) {
            return dadosPF == null ? null : DocumentoValidator.somenteDigitos(dadosPF.getCpf());
        }
        return dadosPJ == null ? null : DocumentoValidator.somenteDigitos(dadosPJ.getCnpj());
    }

    public String documentoFormatado() {
        String doc = documento();
        if (doc == null || doc.isBlank()) {
            return null;
        }
        return tipoPessoa == TipoPessoa.FISICA
                ? DocumentoValidator.formatarCpf(doc)
                : DocumentoValidator.formatarCnpj(doc);
    }

    public boolean isPJ() {
        return tipoPessoa == TipoPessoa.JURIDICA;
    }

    /** Frotista faturado: OS do mes viram uma fatura unica. */
    public boolean isFrotista() {
        return isPJ() && dadosPJ != null && dadosPJ.getCondicoesComerciais() != null
                && dadosPJ.getCondicoesComerciais().isFaturamentoMensal();
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

    public TipoPessoa getTipoPessoa() {
        return tipoPessoa;
    }

    public void setTipoPessoa(TipoPessoa tipoPessoa) {
        this.tipoPessoa = tipoPessoa;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
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

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Instant getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(Instant dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public DadosPF getDadosPF() {
        return dadosPF;
    }

    public void setDadosPF(DadosPF dadosPF) {
        this.dadosPF = dadosPF;
    }

    public DadosPJ getDadosPJ() {
        return dadosPJ;
    }

    public void setDadosPJ(DadosPJ dadosPJ) {
        this.dadosPJ = dadosPJ;
    }

    public boolean isConsentimentoLgpd() {
        return consentimentoLgpd;
    }

    public void setConsentimentoLgpd(boolean consentimentoLgpd) {
        this.consentimentoLgpd = consentimentoLgpd;
    }

    public Instant getConsentimentoEm() {
        return consentimentoEm;
    }

    public void setConsentimentoEm(Instant consentimentoEm) {
        this.consentimentoEm = consentimentoEm;
    }
}
