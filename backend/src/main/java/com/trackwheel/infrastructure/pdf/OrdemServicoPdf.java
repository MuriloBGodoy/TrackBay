package com.trackwheel.infrastructure.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.trackwheel.domain.model.CampoDinamico;
import com.trackwheel.domain.model.ItemPeca;
import com.trackwheel.domain.model.ItemServico;
import com.trackwheel.domain.model.Oficina;
import com.trackwheel.domain.model.OrdemServico;
import com.trackwheel.domain.model.Pagamento;
import com.trackwheel.domain.model.StatusOS;
import com.trackwheel.domain.model.TemplateCampos;
import com.trackwheel.domain.model.TipoCampo;
import com.trackwheel.domain.validation.ContatoValidator;
import com.trackwheel.domain.validation.DocumentoValidator;
import com.trackwheel.domain.validation.PlacaValidator;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Gera o PDF da OS/orcamento: o documento que a oficina imprime ou manda
 * no WhatsApp do cliente. Enquanto a OS esta em ORCAMENTO o titulo e
 * "Orcamento" e ha espaco para assinatura de aprovacao.
 */
@Component
public class OrdemServicoPdf {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(FUSO);
    private static final DateTimeFormatter DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(FUSO);
    private static final Locale PT_BR = Locale.of("pt", "BR");

    private static final Color CINZA_ESCURO = new Color(0x1E, 0x29, 0x3B);
    private static final Color CINZA_MEDIO = new Color(0x64, 0x74, 0x8B);
    private static final Color CINZA_LINHA = new Color(0xE2, 0xE8, 0xF0);
    private static final Color CINZA_FUNDO = new Color(0xF1, 0xF5, 0xF9);

    private static final Font F_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, CINZA_ESCURO);
    private static final Font F_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, CINZA_ESCURO);
    private static final Font F_TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 9, CINZA_ESCURO);
    private static final Font F_TEXTO_NEGRITO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, CINZA_ESCURO);
    private static final Font F_MIUDO = FontFactory.getFont(FontFactory.HELVETICA, 8, CINZA_MEDIO);
    private static final Font F_ROTULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, CINZA_MEDIO);
    private static final Font F_TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, CINZA_ESCURO);

    /** Monta o PDF completo. O template pode ser null (OS sem campos dinamicos). */
    public byte[] gerar(Oficina oficina, OrdemServico os, TemplateCampos template) {
        try (ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 40, 48);
            PdfWriter.getInstance(doc, saida);
            doc.open();

            cabecalho(doc, oficina, os);
            blocoClienteVeiculo(doc, os);
            blocoRelato(doc, os);
            blocoCamposDinamicos(doc, os, template);
            blocoItens(doc, os);
            blocoTotais(doc, os);
            blocoPagamento(doc, os);
            blocoGarantia(doc, os);
            blocoAssinatura(doc, os);
            rodape(doc);

            doc.close();
            return saida.toByteArray();
        } catch (DocumentException | java.io.IOException e) {
            throw new IllegalStateException("Falha ao gerar o PDF da OS " + os.getNumero(), e);
        }
    }

    private void cabecalho(Document doc, Oficina oficina, OrdemServico os) {
        PdfPTable tabela = new PdfPTable(new float[]{3, 2});
        tabela.setWidthPercentage(100);

        PdfPCell esquerda = celulaSemBorda();
        Image logo = carregarLogo(oficina.getLogoUrl());
        if (logo != null) {
            logo.scaleToFit(120, 48);
            esquerda.addElement(logo);
        }
        esquerda.addElement(new Paragraph(ns(oficina.getNomeFantasia()), F_TITULO));
        if (temTexto(oficina.getCnpj())) {
            esquerda.addElement(new Paragraph("CNPJ " + DocumentoValidator.formatarCnpj(oficina.getCnpj()), F_MIUDO));
        }
        if (oficina.getEndereco() != null && temTexto(oficina.getEndereco().resumo())) {
            esquerda.addElement(new Paragraph(oficina.getEndereco().resumo(), F_MIUDO));
        }
        esquerda.addElement(new Paragraph(contatoLinha(oficina), F_MIUDO));
        tabela.addCell(esquerda);

        PdfPCell direita = celulaSemBorda();
        direita.setHorizontalAlignment(Element.ALIGN_RIGHT);
        boolean orcamento = os.getStatus() == StatusOS.ORCAMENTO;
        Paragraph tipo = new Paragraph(orcamento ? "ORÇAMENTO" : "ORDEM DE SERVIÇO", F_SUBTITULO);
        tipo.setAlignment(Element.ALIGN_RIGHT);
        direita.addElement(tipo);
        Paragraph numero = new Paragraph(ns(os.getNumero()), F_TITULO);
        numero.setAlignment(Element.ALIGN_RIGHT);
        direita.addElement(numero);
        Paragraph abertura = new Paragraph("Aberta em " + DATA_HORA.format(os.getDataAbertura()), F_MIUDO);
        abertura.setAlignment(Element.ALIGN_RIGHT);
        direita.addElement(abertura);
        Paragraph status = new Paragraph("Situação: " + rotuloStatus(os.getStatus()), F_MIUDO);
        status.setAlignment(Element.ALIGN_RIGHT);
        direita.addElement(status);
        if (os.getPrevisaoEntrega() != null) {
            Paragraph previsao = new Paragraph("Previsão de entrega: "
                    + DATA_HORA.format(os.getPrevisaoEntrega()), F_MIUDO);
            previsao.setAlignment(Element.ALIGN_RIGHT);
            direita.addElement(previsao);
        }
        tabela.addCell(direita);

        doc.add(tabela);
        doc.add(linhaSeparadora());
    }

    private void blocoClienteVeiculo(Document doc, OrdemServico os) {
        PdfPTable tabela = new PdfPTable(new float[]{1, 1, 1});
        tabela.setWidthPercentage(100);
        tabela.setSpacingBefore(10);

        tabela.addCell(celulaRotuloValor("CLIENTE", ns(os.getClienteNome())));
        String veiculo = PlacaValidator.formatar(ns(os.getVeiculoPlaca()));
        if (temTexto(os.getVeiculoDescricao())) {
            veiculo += " — " + os.getVeiculoDescricao();
        }
        tabela.addCell(celulaRotuloValor("VEÍCULO", veiculo));
        tabela.addCell(celulaRotuloValor("KM DE ENTRADA",
                os.getKmEntrada() == null ? "—" : String.format(PT_BR, "%,d km", os.getKmEntrada())));

        doc.add(tabela);
    }

    private void blocoRelato(Document doc, OrdemServico os) {
        if (!temTexto(os.getReclamacaoCliente()) && !temTexto(os.getDiagnosticoTecnico())) {
            return;
        }
        PdfPTable tabela = new PdfPTable(1);
        tabela.setWidthPercentage(100);
        tabela.setSpacingBefore(8);
        if (temTexto(os.getReclamacaoCliente())) {
            tabela.addCell(celulaRotuloValor("RECLAMAÇÃO DO CLIENTE", os.getReclamacaoCliente()));
        }
        if (temTexto(os.getDiagnosticoTecnico())) {
            tabela.addCell(celulaRotuloValor("DIAGNÓSTICO TÉCNICO", os.getDiagnosticoTecnico()));
        }
        doc.add(tabela);
    }

    private void blocoCamposDinamicos(Document doc, OrdemServico os, TemplateCampos template) {
        if (template == null || os.getCamposDinamicos().isEmpty()) {
            return;
        }
        List<CampoDinamico> visiveis = template.getCampos().stream()
                .filter(c -> c.getTipo() != TipoCampo.FOTO && c.getTipo() != TipoCampo.ASSINATURA)
                .filter(c -> temValor(os.getCamposDinamicos().get(c.getChave())))
                .sorted(Comparator.comparingInt(CampoDinamico::getOrdem))
                .toList();
        if (visiveis.isEmpty()) {
            return;
        }

        doc.add(tituloSecao("Detalhes do serviço"
                + (os.getRamo() == null ? "" : " — " + os.getRamo().getRotulo())));

        PdfPTable tabela = new PdfPTable(new float[]{1, 1});
        tabela.setWidthPercentage(100);
        for (CampoDinamico campo : visiveis) {
            String valor = formatarValorCampo(campo, os.getCamposDinamicos().get(campo.getChave()));
            tabela.addCell(celulaRotuloValor(campo.getRotulo().toUpperCase(PT_BR), valor));
        }
        if (visiveis.size() % 2 != 0) {
            tabela.addCell(celulaSemBorda());
        }
        doc.add(tabela);
    }

    private void blocoItens(Document doc, OrdemServico os) {
        if (os.getItensServico().isEmpty() && os.getItensPeca().isEmpty()) {
            return;
        }
        doc.add(tituloSecao("Itens"));

        PdfPTable tabela = new PdfPTable(new float[]{6, 1.2f, 1.8f, 1.8f});
        tabela.setWidthPercentage(100);
        tabela.setHeaderRows(1);
        tabela.addCell(celulaCabecalhoTabela("Descrição", Element.ALIGN_LEFT));
        tabela.addCell(celulaCabecalhoTabela("Qtd", Element.ALIGN_RIGHT));
        tabela.addCell(celulaCabecalhoTabela("Unitário", Element.ALIGN_RIGHT));
        tabela.addCell(celulaCabecalhoTabela("Total", Element.ALIGN_RIGHT));

        if (!os.getItensServico().isEmpty()) {
            tabela.addCell(celulaSubsecao("Serviços"));
            for (ItemServico item : os.getItensServico()) {
                String descricao = ns(item.getDescricao());
                if (temTexto(item.getMecanicoNome())) {
                    descricao += "  ·  " + item.getMecanicoNome();
                }
                linhaItem(tabela, descricao, item.getQuantidade(), item.getValorUnitario(), item.getTotal());
            }
        }
        if (!os.getItensPeca().isEmpty()) {
            tabela.addCell(celulaSubsecao("Peças"));
            for (ItemPeca item : os.getItensPeca()) {
                String descricao = ns(item.getDescricao());
                if (temTexto(item.getCodigo())) {
                    descricao += "  ·  cód. " + item.getCodigo();
                }
                linhaItem(tabela, descricao, item.getQuantidade(), item.getValorUnitario(), item.getTotal());
            }
        }
        doc.add(tabela);
    }

    private void blocoTotais(Document doc, OrdemServico os) {
        PdfPTable tabela = new PdfPTable(new float[]{7, 3});
        tabela.setWidthPercentage(100);
        tabela.setSpacingBefore(6);

        tabela.addCell(celulaSemBorda());
        PdfPCell resumo = celulaSemBorda();

        PdfPTable linhas = new PdfPTable(new float[]{1, 1});
        linhas.setWidthPercentage(100);
        linhaTotal(linhas, "Serviços", moeda(os.getSubtotalServicos()), F_TEXTO);
        linhaTotal(linhas, "Peças", moeda(os.getSubtotalPecas()), F_TEXTO);
        if (positivo(os.getDescontoGeral())) {
            linhaTotal(linhas, "Desconto", "− " + moeda(os.getDescontoGeral()), F_TEXTO);
        }
        if (positivo(os.getAcrescimo())) {
            linhaTotal(linhas, "Acréscimo", moeda(os.getAcrescimo()), F_TEXTO);
        }
        linhaTotal(linhas, "TOTAL", moeda(os.getTotal()), F_TOTAL);

        resumo.addElement(linhas);
        tabela.addCell(resumo);
        doc.add(tabela);
    }

    private void blocoPagamento(Document doc, OrdemServico os) {
        Pagamento pagamento = os.getPagamento();
        if (pagamento == null || pagamento.getParcelas().isEmpty()) {
            return;
        }
        doc.add(tituloSecao("Pagamento"));
        PdfPTable tabela = new PdfPTable(new float[]{4, 3, 3});
        tabela.setWidthPercentage(100);
        for (Pagamento.Parcela parcela : pagamento.getParcelas()) {
            String forma = rotuloForma(parcela.getForma());
            if (parcela.getNumeroParcelas() > 1) {
                forma += " " + parcela.getNumeroParcelas() + "x";
            }
            tabela.addCell(celulaTexto(forma, F_TEXTO, Element.ALIGN_LEFT));
            tabela.addCell(celulaTexto(
                    parcela.getVencimento() == null ? "" : "vence " + DATA.format(parcela.getVencimento()),
                    F_MIUDO, Element.ALIGN_LEFT));
            tabela.addCell(celulaTexto(moeda(parcela.getValor()), F_TEXTO_NEGRITO, Element.ALIGN_RIGHT));
        }
        doc.add(tabela);
    }

    private void blocoGarantia(Document doc, OrdemServico os) {
        boolean temPrazo = os.getGarantiaDias() != null || os.getGarantiaKm() != null;
        if (!temTexto(os.getTextoGarantia()) && !temPrazo) {
            return;
        }
        doc.add(tituloSecao("Garantia"));
        if (temPrazo) {
            StringBuilder prazo = new StringBuilder();
            if (os.getGarantiaDias() != null) {
                prazo.append(os.getGarantiaDias()).append(" dias");
            }
            if (os.getGarantiaKm() != null) {
                if (!prazo.isEmpty()) {
                    prazo.append(" ou ");
                }
                prazo.append(String.format(PT_BR, "%,d km", os.getGarantiaKm()))
                        .append(" (o que ocorrer primeiro)");
            }
            doc.add(new Paragraph(prazo.toString(), F_TEXTO_NEGRITO));
        }
        if (temTexto(os.getTextoGarantia())) {
            doc.add(new Paragraph(os.getTextoGarantia(), F_MIUDO));
        }
    }

    private void blocoAssinatura(Document doc, OrdemServico os) {
        if (os.getAssinadaEm() != null) {
            Paragraph aprovado = new Paragraph(
                    "Orçamento aprovado pelo cliente em " + DATA_HORA.format(os.getAssinadaEm()) + ".", F_TEXTO);
            aprovado.setSpacingBefore(24);
            doc.add(aprovado);
            return;
        }
        if (os.getStatus() != StatusOS.ORCAMENTO) {
            return;
        }
        Paragraph linha = new Paragraph("_________________________________________", F_TEXTO);
        linha.setSpacingBefore(40);
        linha.setAlignment(Element.ALIGN_CENTER);
        doc.add(linha);
        Paragraph legenda = new Paragraph("Assinatura do cliente — aprovação do orçamento", F_MIUDO);
        legenda.setAlignment(Element.ALIGN_CENTER);
        doc.add(legenda);
    }

    private void rodape(Document doc) {
        Paragraph rodape = new Paragraph(
                "Documento gerado em " + DATA_HORA.format(Instant.now()) + " · Track Wheel", F_MIUDO);
        rodape.setSpacingBefore(24);
        rodape.setAlignment(Element.ALIGN_CENTER);
        doc.add(rodape);
    }

    // ---- helpers de formato ----

    private String formatarValorCampo(CampoDinamico campo, Object valor) {
        if (valor instanceof List<?> lista) {
            return String.join(", ", lista.stream().map(String::valueOf).toList());
        }
        return switch (campo.getTipo()) {
            case BOOLEANO -> Boolean.parseBoolean(String.valueOf(valor)) ? "Sim" : "Não";
            case MOEDA -> valor instanceof Number n ? moeda(new BigDecimal(n.toString())) : String.valueOf(valor);
            case DATA -> formatarDataIso(String.valueOf(valor));
            case NUMERO, DECIMAL -> String.valueOf(valor)
                    + (temTexto(campo.getUnidade()) ? " " + campo.getUnidade() : "");
            default -> String.valueOf(valor);
        };
    }

    private String formatarDataIso(String valor) {
        try {
            return DATA.format(LocalDate.parse(valor));
        } catch (Exception e) {
            return valor;
        }
    }

    private String moeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(valor == null ? BigDecimal.ZERO : valor);
    }

    private String rotuloStatus(StatusOS status) {
        return switch (status) {
            case ORCAMENTO -> "Orçamento";
            case APROVADA -> "Aprovada";
            case EM_EXECUCAO -> "Em execução";
            case AGUARDANDO_PECA -> "Aguardando peça";
            case PRONTA -> "Pronta";
            case ENTREGUE -> "Entregue";
            case CANCELADA -> "Cancelada";
        };
    }

    private String rotuloForma(com.trackwheel.domain.model.FormaPagamento forma) {
        if (forma == null) {
            return "";
        }
        return switch (forma) {
            case DINHEIRO -> "Dinheiro";
            case PIX -> "PIX";
            case DEBITO -> "Débito";
            case CREDITO -> "Crédito";
            case BOLETO -> "Boleto";
            case TRANSFERENCIA -> "Transferência";
            case FATURADO -> "Faturado";
            case CHEQUE -> "Cheque";
        };
    }

    private String contatoLinha(Oficina oficina) {
        StringBuilder sb = new StringBuilder();
        if (temTexto(oficina.getTelefone())) {
            sb.append(ContatoValidator.formatarTelefone(oficina.getTelefone()));
        }
        if (temTexto(oficina.getWhatsapp())) {
            if (!sb.isEmpty()) {
                sb.append(" · ");
            }
            sb.append("WhatsApp ").append(ContatoValidator.formatarTelefone(oficina.getWhatsapp()));
        }
        if (temTexto(oficina.getEmail())) {
            if (!sb.isEmpty()) {
                sb.append(" · ");
            }
            sb.append(oficina.getEmail());
        }
        return sb.toString();
    }

    private Image carregarLogo(String logoUrl) {
        if (!temTexto(logoUrl)) {
            return null;
        }
        try {
            // Aceita data-URI (base64) ou URL; se falhar, o PDF sai sem logo.
            if (logoUrl.startsWith("data:")) {
                String base64 = logoUrl.substring(logoUrl.indexOf(',') + 1);
                return Image.getInstance(java.util.Base64.getDecoder().decode(base64));
            }
            return Image.getInstance(java.net.URI.create(logoUrl).toURL());
        } catch (Exception e) {
            return null;
        }
    }

    // ---- helpers de layout ----

    private void linhaItem(PdfPTable tabela, String descricao, BigDecimal quantidade,
                           BigDecimal valorUnitario, BigDecimal total) {
        tabela.addCell(celulaTexto(descricao, F_TEXTO, Element.ALIGN_LEFT));
        tabela.addCell(celulaTexto(quantidadeCurta(quantidade), F_TEXTO, Element.ALIGN_RIGHT));
        tabela.addCell(celulaTexto(moeda(valorUnitario), F_TEXTO, Element.ALIGN_RIGHT));
        tabela.addCell(celulaTexto(moeda(total), F_TEXTO_NEGRITO, Element.ALIGN_RIGHT));
    }

    private String quantidadeCurta(BigDecimal quantidade) {
        if (quantidade == null) {
            return "1";
        }
        return quantidade.stripTrailingZeros().toPlainString().replace('.', ',');
    }

    private void linhaTotal(PdfPTable tabela, String rotulo, String valor, Font fonte) {
        PdfPCell c1 = celulaSemBorda();
        c1.setPhrase(new Phrase(rotulo, fonte == F_TOTAL ? F_TOTAL : F_MIUDO));
        tabela.addCell(c1);
        PdfPCell c2 = celulaSemBorda();
        c2.setPhrase(new Phrase(valor, fonte));
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabela.addCell(c2);
    }

    private Paragraph tituloSecao(String texto) {
        Paragraph p = new Paragraph(texto, F_SUBTITULO);
        p.setSpacingBefore(14);
        p.setSpacingAfter(4);
        return p;
    }

    private PdfPCell celulaSemBorda() {
        PdfPCell celula = new PdfPCell();
        celula.setBorder(Rectangle.NO_BORDER);
        celula.setPadding(2);
        return celula;
    }

    private PdfPCell celulaTexto(String texto, Font fonte, int alinhamento) {
        PdfPCell celula = new PdfPCell(new Phrase(ns(texto), fonte));
        celula.setBorder(Rectangle.BOTTOM);
        celula.setBorderColor(CINZA_LINHA);
        celula.setPadding(5);
        celula.setHorizontalAlignment(alinhamento);
        return celula;
    }

    private PdfPCell celulaCabecalhoTabela(String texto, int alinhamento) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, F_ROTULO));
        celula.setBackgroundColor(CINZA_FUNDO);
        celula.setBorder(Rectangle.NO_BORDER);
        celula.setPadding(5);
        celula.setHorizontalAlignment(alinhamento);
        return celula;
    }

    private PdfPCell celulaSubsecao(String texto) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, F_TEXTO_NEGRITO));
        celula.setColspan(4);
        celula.setBorder(Rectangle.NO_BORDER);
        celula.setPaddingTop(8);
        celula.setPaddingBottom(3);
        celula.setPaddingLeft(5);
        return celula;
    }

    private PdfPCell celulaRotuloValor(String rotulo, String valor) {
        PdfPCell celula = celulaSemBorda();
        celula.setPadding(4);
        celula.addElement(new Paragraph(rotulo, F_ROTULO));
        celula.addElement(new Paragraph(ns(valor), F_TEXTO));
        return celula;
    }

    private PdfPTable linhaSeparadora() {
        PdfPTable tabela = new PdfPTable(1);
        tabela.setWidthPercentage(100);
        tabela.setSpacingBefore(8);
        PdfPCell celula = new PdfPCell();
        celula.setFixedHeight(1);
        celula.setBackgroundColor(CINZA_LINHA);
        celula.setBorder(Rectangle.NO_BORDER);
        tabela.addCell(celula);
        return tabela;
    }

    // ---- helpers gerais ----

    private static boolean temTexto(String s) {
        return s != null && !s.isBlank();
    }

    private static boolean temValor(Object valor) {
        return valor != null
                && !(valor instanceof String s && s.isBlank())
                && !(valor instanceof List<?> l && l.isEmpty());
    }

    private static boolean positivo(BigDecimal v) {
        return v != null && v.compareTo(BigDecimal.ZERO) > 0;
    }

    private static String ns(String s) {
        return s == null ? "" : s;
    }
}
