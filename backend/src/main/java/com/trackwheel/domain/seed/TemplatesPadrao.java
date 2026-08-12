package com.trackwheel.domain.seed;

import com.trackwheel.domain.model.CampoDinamico;
import com.trackwheel.domain.model.Ramo;
import com.trackwheel.domain.model.TipoCampo;
import com.trackwheel.domain.model.TipoVeiculo;

import java.util.ArrayList;
import java.util.List;

/**
 * Catalogo de campos que ja vem pronto por ramo. A oficina nunca comeca do zero:
 * escolhe o ramo no onboarding e recebe estes campos, que depois pode editar.
 */
public final class TemplatesPadrao {

    private TemplatesPadrao() {
    }

    public static List<CampoDinamico> para(Ramo ramo) {
        return switch (ramo) {
            case RADIADOR -> radiador();
            case MECANICA_GERAL -> mecanicaGeral();
            case FUNILARIA_PINTURA -> funilariaPintura();
            case ELETRICA -> eletrica();
            case SUSPENSAO_FREIOS -> suspensaoFreios();
            case AR_CONDICIONADO -> arCondicionado();
            case TROCA_OLEO -> trocaOleo();
            case PNEUS_ALINHAMENTO -> pneusAlinhamento();
            case INJECAO_ELETRONICA -> injecaoEletronica();
            case OUTRO -> outro();
        };
    }

    private static CampoDinamico campo(String chave, String rotulo, TipoCampo tipo, boolean obrigatorio,
                                       List<String> opcoes, int ordem, String grupo) {
        return new CampoDinamico(chave, rotulo, tipo, obrigatorio, opcoes, ordem, grupo);
    }

    private static List<CampoDinamico> radiador() {
        List<CampoDinamico> c = new ArrayList<>();
        c.add(campo("tipo_radiador", "Tipo de radiador", TipoCampo.SELECT, true,
                List.of("Cobre/latao", "Aluminio", "Aluminio/plastico"), 1, "Diagnostico"));
        c.add(campo("houve_superaquecimento", "Houve superaquecimento?", TipoCampo.BOOLEANO, false,
                null, 2, "Diagnostico"));
        c.add(campo("temperatura_atingida", "Temperatura atingida (C)", TipoCampo.NUMERO, false,
                null, 3, "Diagnostico").comCondicional("houve_superaquecimento", true).comUnidade("C"));
        c.add(campo("houve_vazamento", "Houve vazamento?", TipoCampo.BOOLEANO, false,
                null, 4, "Diagnostico"));
        c.add(campo("local_vazamento", "Local do vazamento", TipoCampo.SELECT, false,
                List.of("Colmeia", "Tanque superior", "Tanque inferior", "Mangueira", "Bomba d'agua",
                        "Reservatorio", "Tampa", "Valvula termostatica", "Nao identificado"),
                5, "Diagnostico").comCondicional("houve_vazamento", true));
        c.add(campo("teste_pressao", "Teste de pressao (bar)", TipoCampo.DECIMAL, false,
                null, 6, "Diagnostico").comUnidade("bar"));
        c.add(campo("estado_reservatorio", "Estado do reservatorio", TipoCampo.SELECT, false,
                List.of("Bom", "Trincado", "Sujo/oxidado", "Substituir"), 7, "Inspecao"));
        c.add(campo("estado_tampa", "Estado da tampa", TipoCampo.SELECT, false,
                List.of("Boa", "Mola fraca", "Vedacao ruim", "Substituir"), 8, "Inspecao"));
        c.add(campo("estado_mangueiras", "Estado das mangueiras", TipoCampo.SELECT, false,
                List.of("Boas", "Ressecadas", "Inchadas", "Substituir"), 9, "Inspecao"));
        c.add(campo("estado_eletroventilador", "Estado do eletroventilador", TipoCampo.SELECT, false,
                List.of("Funcionando", "Intermitente", "Nao liga", "Ruido no mancal"), 10, "Inspecao"));
        c.add(campo("servico_executado", "Servico executado", TipoCampo.MULTI_SELECT, true,
                List.of("Limpeza quimica", "Desobstrucao", "Solda", "Troca de colmeia",
                        "Recuperacao", "Troca completa", "Troca de tampa", "Troca de mangueiras"),
                11, "Servico"));
        c.add(campo("tipo_aditivo", "Tipo de aditivo", TipoCampo.SELECT, false,
                List.of("Organico (OAT) rosa", "Organico (OAT) laranja", "Hibrido (HOAT) amarelo",
                        "Inorganico verde", "Concentrado", "Pronto uso"), 12, "Servico"));
        c.add(campo("quantidade_aditivo", "Quantidade de aditivo (L)", TipoCampo.DECIMAL, false,
                null, 13, "Servico").comUnidade("L"));
        c.add(campo("foto_radiador", "Foto do radiador", TipoCampo.FOTO, false, null, 14, "Evidencias"));
        c.add(campo("observacoes_tecnicas", "Observacoes tecnicas", TipoCampo.TEXTO_LONGO, false,
                null, 15, "Evidencias"));
        return c;
    }

    private static List<CampoDinamico> mecanicaGeral() {
        List<CampoDinamico> c = new ArrayList<>();
        c.add(campo("sistema_afetado", "Sistema afetado", TipoCampo.MULTI_SELECT, true,
                List.of("Motor", "Cambio", "Embreagem", "Suspensao", "Freios", "Direcao",
                        "Arrefecimento", "Escapamento", "Eletrico", "Transmissao"), 1, "Diagnostico"));
        c.add(campo("ruidos", "Ruidos observados", TipoCampo.MULTI_SELECT, false,
                List.of("Batida seca", "Chiado", "Assobio", "Estalo", "Zumbido", "Trepidacao", "Nenhum"),
                2, "Diagnostico"));
        c.add(campo("quando_ocorre", "Quando ocorre", TipoCampo.SELECT, false,
                List.of("Motor frio", "Motor quente", "Em aceleracao", "Em frenagem", "Em curva",
                        "Acima de 60 km/h", "Constante", "Intermitente"), 3, "Diagnostico"));
        c.add(campo("revisao_preventiva", "Revisao preventiva", TipoCampo.CHECKLIST, false,
                List.of("Oleo do motor", "Filtro de oleo", "Filtro de ar", "Filtro de combustivel",
                        "Filtro de cabine", "Velas", "Correia dentada", "Correia do alternador",
                        "Fluido de freio", "Fluido de arrefecimento", "Bateria", "Pneus"),
                4, "Preventiva"));
        c.add(campo("correias_trocadas", "Correias trocadas", TipoCampo.MULTI_SELECT, false,
                List.of("Dentada", "Alternador", "Ar-condicionado", "Direcao hidraulica", "Nenhuma"),
                5, "Servico"));
        c.add(campo("fluidos_trocados", "Fluidos trocados", TipoCampo.MULTI_SELECT, false,
                List.of("Oleo do motor", "Oleo do cambio", "Fluido de freio", "Fluido de direcao",
                        "Aditivo de arrefecimento", "Nenhum"), 6, "Servico"));
        c.add(campo("km_proxima_revisao", "Km da proxima revisao", TipoCampo.NUMERO, false,
                null, 7, "Servico").comUnidade("km"));
        c.add(campo("observacoes_tecnicas", "Observacoes tecnicas", TipoCampo.TEXTO_LONGO, false,
                null, 8, "Evidencias"));
        return c;
    }

    private static List<CampoDinamico> funilariaPintura() {
        List<CampoDinamico> c = new ArrayList<>();
        c.add(campo("pecas_afetadas", "Pecas afetadas", TipoCampo.MULTI_SELECT, true,
                List.of("Para-choque dianteiro", "Para-choque traseiro", "Capo", "Porta dianteira esquerda",
                        "Porta dianteira direita", "Porta traseira esquerda", "Porta traseira direita",
                        "Paralama esquerdo", "Paralama direito", "Teto", "Tampa traseira", "Lateral",
                        "Coluna", "Retrovisor"), 1, "Avaliacao"));
        c.add(campo("tipo_dano", "Tipo de dano", TipoCampo.SELECT, true,
                List.of("Amassado leve", "Amassado profundo", "Risco superficial", "Risco profundo",
                        "Trinca", "Corrosao/ferrugem", "Perda de peca", "Repintura"), 2, "Avaliacao"));
        c.add(campo("cor_veiculo", "Cor do veiculo", TipoCampo.TEXTO, false, null, 3, "Pintura"));
        c.add(campo("codigo_tinta", "Codigo da tinta", TipoCampo.TEXTO, false, null, 4, "Pintura"));
        c.add(campo("tipo_pintura", "Tipo de pintura", TipoCampo.SELECT, false,
                List.of("Solida", "Metalica", "Perolizada", "Fosca", "Tricoat"), 5, "Pintura"));
        c.add(campo("precisa_polimento", "Precisa polimento?", TipoCampo.BOOLEANO, false, null, 6, "Pintura"));
        c.add(campo("e_sinistro", "E sinistro?", TipoCampo.BOOLEANO, false, null, 7, "Sinistro"));
        c.add(campo("seguradora", "Seguradora", TipoCampo.TEXTO, false, null, 8, "Sinistro")
                .comCondicional("e_sinistro", true));
        c.add(campo("numero_sinistro", "Numero do sinistro", TipoCampo.TEXTO, false, null, 9, "Sinistro")
                .comCondicional("e_sinistro", true));
        c.add(campo("foto_antes", "Foto antes", TipoCampo.FOTO, false, null, 10, "Evidencias"));
        c.add(campo("foto_depois", "Foto depois", TipoCampo.FOTO, false, null, 11, "Evidencias"));
        return c;
    }

    private static List<CampoDinamico> eletrica() {
        List<CampoDinamico> c = new ArrayList<>();
        c.add(campo("componente", "Componente", TipoCampo.MULTI_SELECT, true,
                List.of("Bateria", "Alternador", "Motor de partida", "Chicote", "Fusiveis", "Reles",
                        "Farol", "Lanterna", "Setas", "Vidro eletrico", "Trava eletrica", "Central",
                        "Sensor", "Buzina"), 1, "Diagnostico"));
        c.add(campo("teste_bateria", "Teste de bateria (V)", TipoCampo.DECIMAL, false,
                null, 2, "Testes").comUnidade("V"));
        c.add(campo("bateria_amperagem", "Amperagem da bateria (Ah)", TipoCampo.NUMERO, false,
                null, 3, "Testes").comUnidade("Ah"));
        c.add(campo("teste_alternador", "Carga do alternador (A)", TipoCampo.DECIMAL, false,
                null, 4, "Testes").comUnidade("A"));
        c.add(campo("tensao_carga", "Tensao em carga (V)", TipoCampo.DECIMAL, false,
                null, 5, "Testes").comUnidade("V"));
        c.add(campo("codigo_falha_obd", "Codigo de falha (OBD)", TipoCampo.TEXTO, false,
                null, 6, "Diagnostico"));
        c.add(campo("acessorios_instalados", "Acessorios instalados", TipoCampo.MULTI_SELECT, false,
                List.of("Som", "Alarme", "Rastreador", "Camera de re", "Sensor de estacionamento",
                        "Farol de milha", "Vidro eletrico", "Trava eletrica", "Nenhum"), 7, "Servico"));
        c.add(campo("observacoes_tecnicas", "Observacoes tecnicas", TipoCampo.TEXTO_LONGO, false,
                null, 8, "Evidencias"));
        return c;
    }

    private static List<CampoDinamico> suspensaoFreios() {
        List<CampoDinamico> c = new ArrayList<>();
        c.add(campo("componente", "Componente", TipoCampo.MULTI_SELECT, true,
                List.of("Amortecedor dianteiro", "Amortecedor traseiro", "Mola", "Batente", "Coxim",
                        "Bandeja", "Pivo", "Terminal de direcao", "Bieleta", "Barra estabilizadora",
                        "Pastilha", "Disco", "Lona", "Tambor", "Cilindro", "Fluido de freio"),
                1, "Diagnostico"));
        c.add(campo("espessura_pastilha_dianteira", "Espessura da pastilha dianteira (mm)", TipoCampo.DECIMAL,
                false, null, 2, "Medicoes").comUnidade("mm"));
        c.add(campo("espessura_pastilha_traseira", "Espessura da pastilha traseira (mm)", TipoCampo.DECIMAL,
                false, null, 3, "Medicoes").comUnidade("mm"));
        c.add(campo("estado_disco", "Estado do disco", TipoCampo.SELECT, false,
                List.of("Bom", "Sulcado", "Empenado", "Abaixo da medida minima", "Substituir"),
                4, "Medicoes"));
        c.add(campo("espessura_disco", "Espessura do disco (mm)", TipoCampo.DECIMAL, false,
                null, 5, "Medicoes").comUnidade("mm"));
        c.add(campo("necessita_alinhamento", "Necessita alinhamento?", TipoCampo.BOOLEANO, false,
                null, 6, "Servico"));
        c.add(campo("necessita_balanceamento", "Necessita balanceamento?", TipoCampo.BOOLEANO, false,
                null, 7, "Servico"));
        c.add(campo("observacoes_tecnicas", "Observacoes tecnicas", TipoCampo.TEXTO_LONGO, false,
                null, 8, "Evidencias"));
        return c;
    }

    private static List<CampoDinamico> arCondicionado() {
        List<CampoDinamico> c = new ArrayList<>();
        c.add(campo("tipo_gas", "Tipo de gas", TipoCampo.SELECT, true,
                List.of("R-134a", "R-1234yf", "R-12"), 1, "Servico"));
        c.add(campo("carga_gas", "Carga de gas (g)", TipoCampo.NUMERO, false,
                null, 2, "Servico").comUnidade("g"));
        c.add(campo("pressao_alta", "Pressao lado alta (PSI)", TipoCampo.DECIMAL, false,
                null, 3, "Medicoes").comUnidade("PSI"));
        c.add(campo("pressao_baixa", "Pressao lado baixa (PSI)", TipoCampo.DECIMAL, false,
                null, 4, "Medicoes").comUnidade("PSI"));
        c.add(campo("temperatura_saida", "Temperatura na saida (C)", TipoCampo.DECIMAL, false,
                null, 5, "Medicoes").comUnidade("C"));
        c.add(campo("higienizacao", "Higienizacao realizada?", TipoCampo.BOOLEANO, false, null, 6, "Servico"));
        c.add(campo("teste_estanqueidade", "Teste de estanqueidade", TipoCampo.SELECT, false,
                List.of("Aprovado", "Vazamento no condensador", "Vazamento no evaporador",
                        "Vazamento na mangueira", "Vazamento no compressor", "Nao realizado"),
                7, "Testes"));
        c.add(campo("componentes_trocados", "Componentes trocados", TipoCampo.MULTI_SELECT, false,
                List.of("Compressor", "Condensador", "Evaporador", "Filtro secador", "Valvula de expansao",
                        "Mangueira", "Filtro de cabine", "Nenhum"), 8, "Servico"));
        return c;
    }

    private static List<CampoDinamico> trocaOleo() {
        List<CampoDinamico> c = new ArrayList<>();
        c.add(campo("tipo_oleo", "Tipo de oleo", TipoCampo.SELECT, true,
                List.of("Mineral", "Semissintetico", "Sintetico"), 1, "Servico"));
        c.add(campo("viscosidade", "Viscosidade", TipoCampo.SELECT, true,
                List.of("0W20", "0W30", "5W30", "5W40", "10W40", "15W40", "20W50"), 2, "Servico"));
        c.add(campo("marca_oleo", "Marca do oleo", TipoCampo.TEXTO, false, null, 3, "Servico"));
        c.add(campo("quantidade_litros", "Quantidade (L)", TipoCampo.DECIMAL, true,
                null, 4, "Servico").comUnidade("L"));
        c.add(campo("filtros_trocados", "Filtros trocados", TipoCampo.MULTI_SELECT, false,
                List.of("Oleo", "Ar", "Combustivel", "Cabine", "Nenhum"), 5, "Servico"));
        c.add(campo("km_proxima_troca", "Km da proxima troca", TipoCampo.NUMERO, true,
                null, 6, "Proxima troca").comUnidade("km"));
        c.add(campo("data_proxima_troca", "Data da proxima troca", TipoCampo.DATA, false,
                null, 7, "Proxima troca"));
        return c;
    }

    private static List<CampoDinamico> pneusAlinhamento() {
        List<CampoDinamico> c = new ArrayList<>();
        c.add(campo("medida_pneu", "Medida do pneu", TipoCampo.TEXTO, true, null, 1, "Pneus"));
        c.add(campo("marca_pneu", "Marca do pneu", TipoCampo.TEXTO, false, null, 2, "Pneus"));
        c.add(campo("posicoes_trocadas", "Posicoes trocadas", TipoCampo.MULTI_SELECT, false,
                List.of("Dianteiro esquerdo", "Dianteiro direito", "Traseiro esquerdo",
                        "Traseiro direito", "Estepe"), 3, "Pneus"));
        c.add(campo("sulco_de", "Sulco dianteiro esquerdo (mm)", TipoCampo.DECIMAL, false,
                null, 4, "Medicoes").comUnidade("mm"));
        c.add(campo("sulco_dd", "Sulco dianteiro direito (mm)", TipoCampo.DECIMAL, false,
                null, 5, "Medicoes").comUnidade("mm"));
        c.add(campo("sulco_te", "Sulco traseiro esquerdo (mm)", TipoCampo.DECIMAL, false,
                null, 6, "Medicoes").comUnidade("mm"));
        c.add(campo("sulco_td", "Sulco traseiro direito (mm)", TipoCampo.DECIMAL, false,
                null, 7, "Medicoes").comUnidade("mm"));
        c.add(campo("cambagem_antes", "Cambagem antes", TipoCampo.TEXTO, false, null, 8, "Alinhamento"));
        c.add(campo("cambagem_depois", "Cambagem depois", TipoCampo.TEXTO, false, null, 9, "Alinhamento"));
        c.add(campo("caster_antes", "Caster antes", TipoCampo.TEXTO, false, null, 10, "Alinhamento"));
        c.add(campo("caster_depois", "Caster depois", TipoCampo.TEXTO, false, null, 11, "Alinhamento"));
        c.add(campo("convergencia_antes", "Convergencia antes", TipoCampo.TEXTO, false, null, 12, "Alinhamento"));
        c.add(campo("convergencia_depois", "Convergencia depois", TipoCampo.TEXTO, false, null, 13, "Alinhamento"));
        c.add(campo("balanceamento", "Balanceamento realizado?", TipoCampo.BOOLEANO, false, null, 14, "Servico"));
        return c;
    }

    private static List<CampoDinamico> injecaoEletronica() {
        List<CampoDinamico> c = new ArrayList<>();
        c.add(campo("codigos_falha", "Codigos de falha", TipoCampo.TEXTO_LONGO, false,
                null, 1, "Diagnostico"));
        c.add(campo("scanner_usado", "Scanner usado", TipoCampo.TEXTO, false, null, 2, "Diagnostico"));
        c.add(campo("teste_bicos", "Teste de bicos", TipoCampo.SELECT, false,
                List.of("Vazao uniforme", "Vazao irregular", "Bico entupido", "Bico vazando",
                        "Nao realizado"), 3, "Testes"));
        c.add(campo("limpeza_realizada", "Limpeza realizada", TipoCampo.MULTI_SELECT, false,
                List.of("Bicos injetores", "Corpo de borboleta", "Valvula EGR", "Sensor MAF",
                        "Tanque", "Nenhuma"), 4, "Servico"));
        c.add(campo("pressao_combustivel", "Pressao de combustivel (bar)", TipoCampo.DECIMAL, false,
                null, 5, "Testes").comUnidade("bar"));
        c.add(campo("apagou_falhas", "Apagou as falhas?", TipoCampo.BOOLEANO, false, null, 6, "Servico"));
        c.add(campo("observacoes_tecnicas", "Observacoes tecnicas", TipoCampo.TEXTO_LONGO, false,
                null, 7, "Evidencias"));
        return c;
    }

    private static List<CampoDinamico> outro() {
        List<CampoDinamico> c = new ArrayList<>();
        c.add(campo("descricao_servico", "Descricao do servico", TipoCampo.TEXTO_LONGO, true,
                null, 1, "Servico"));
        c.add(campo("observacoes_tecnicas", "Observacoes tecnicas", TipoCampo.TEXTO_LONGO, false,
                null, 2, "Evidencias"));
        return c;
    }

    /** Campos que so fazem sentido em alguns tipos de veiculo. */
    public static List<CampoDinamico> comFiltroDeVeiculo(List<CampoDinamico> campos, Ramo ramo) {
        if (ramo == Ramo.RADIADOR) {
            campos.stream()
                    .filter(c -> "teste_pressao".equals(c.getChave()))
                    .forEach(c -> c.comAplicavelA(TipoVeiculo.CARRO, TipoVeiculo.CAMINHAO,
                            TipoVeiculo.VAN, TipoVeiculo.MAQUINA));
        }
        return campos;
    }
}
