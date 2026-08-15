package com.trackwheel.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Onde o objeto esta e ate quando ele pode ficar. Status e lugar sao eixos
 * independentes — mover nao muda a etapa do trabalho, e vice-versa.
 */
class LocalizacaoOrdemServicoTest {

    @Test
    @DisplayName("OS nasce no patio")
    void nasceNoPatio() {
        OrdemServico os = new OrdemServico();

        assertEquals(LocalOficina.PATIO, os.getLocal());
        assertEquals("Patio", os.localRotulo());
        assertTrue(os.getHistoricoLocal().isEmpty());
    }

    @Test
    @DisplayName("mover grava de onde veio, para onde foi e quem moveu")
    void movimentacaoEntraNoHistorico() {
        OrdemServico os = new OrdemServico();

        os.moverPara(LocalOficina.ELEVADOR, null, "u1", "Joao");

        assertEquals(LocalOficina.ELEVADOR, os.getLocal());
        assertEquals(1, os.getHistoricoLocal().size());
        OrdemServico.MovimentacaoLocal m = os.getHistoricoLocal().get(0);
        assertEquals(LocalOficina.PATIO, m.de());
        assertEquals(LocalOficina.ELEVADOR, m.para());
        assertEquals("Joao", m.autorNome());
    }

    @Test
    @DisplayName("mover para o mesmo lugar nao suja o historico")
    void moverParaOndeJaEstaEhNoOp() {
        OrdemServico os = new OrdemServico();
        os.moverPara(LocalOficina.DUCHA, null, "u1", "Joao");

        os.moverPara(LocalOficina.DUCHA, null, "u1", "Joao");

        assertEquals(1, os.getHistoricoLocal().size());
    }

    @Test
    @DisplayName("OUTRO sem dizer onde e recusado")
    void outroExigeDetalhe() {
        OrdemServico os = new OrdemServico();

        assertThrows(IllegalArgumentException.class,
                () -> os.moverPara(LocalOficina.OUTRO, "  ", "u1", "Joao"));
    }

    @Test
    @DisplayName("OUTRO usa o nome que a oficina deu ao canto")
    void outroUsaODetalheComoRotulo() {
        OrdemServico os = new OrdemServico();

        os.moverPara(LocalOficina.OUTRO, "Fundo do galpao", "u1", "Joao");

        assertEquals("Fundo do galpao", os.localRotulo());
    }

    @Test
    @DisplayName("detalhe de OUTRO nao vaza para um lugar que nao usa detalhe")
    void detalheSoValeParaOutro() {
        OrdemServico os = new OrdemServico();
        os.moverPara(LocalOficina.OUTRO, "Fundo do galpao", "u1", "Joao");

        os.moverPara(LocalOficina.BOX, "ignorado", "u1", "Joao");

        assertNull(os.getLocalDetalhe());
        assertEquals("Box", os.localRotulo());
    }

    @Test
    @DisplayName("passou da previsao e ainda esta na oficina: atrasada")
    void atrasaQuandoPassaDaPrevisao() {
        OrdemServico os = new OrdemServico();
        os.setPrevisaoEntrega(Instant.now().minus(1, ChronoUnit.HOURS));

        assertTrue(os.atrasada());
    }

    @Test
    @DisplayName("OS entregue nunca atrasa, mesmo com previsao vencida")
    void entregueNaoAtrasa() {
        OrdemServico os = new OrdemServico();
        os.setPrevisaoEntrega(Instant.now().minus(1, ChronoUnit.HOURS));
        os.transicionarPara(StatusOS.APROVADA, "u1", "Joao", null);
        os.transicionarPara(StatusOS.EM_EXECUCAO, "u1", "Joao", null);
        os.transicionarPara(StatusOS.PRONTA, "u1", "Joao", null);
        os.transicionarPara(StatusOS.ENTREGUE, "u1", "Joao", null);

        assertFalse(os.atrasada());
    }

    @Test
    @DisplayName("sem previsao nao existe atraso")
    void semPrevisaoNaoAtrasa() {
        assertFalse(new OrdemServico().atrasada());
    }

    @Test
    @DisplayName("peca avulsa descreve o objeto; veiculo descreve o carro")
    void descricaoDoObjetoSegueOTipo() {
        OrdemServico veiculo = new OrdemServico();
        veiculo.setVeiculoDescricao("Volkswagen Gol 2016");

        OrdemServico peca = new OrdemServico();
        peca.setTipoObjeto(TipoObjeto.PECA);
        peca.setObjetoDescricao("Radiador Gol G5");

        assertEquals("Volkswagen Gol 2016", veiculo.descricaoObjeto());
        assertEquals("Radiador Gol G5", peca.descricaoObjeto());
    }
}
