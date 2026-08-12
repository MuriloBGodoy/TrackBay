package com.trackwheel.infrastructure.firestore;

import com.trackwheel.domain.model.Endereco;
import com.trackwheel.domain.model.ItemPeca;
import com.trackwheel.domain.model.Oficina;
import com.trackwheel.domain.model.OrdemServico;
import com.trackwheel.domain.model.Ramo;
import com.trackwheel.domain.model.StatusOS;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConversorFirestoreTest {

    private final ConversorFirestore conversor = new ConversorFirestore();

    @Test
    @DisplayName("mapa gerado so contem tipos que o Firestore aceita")
    void mapaSemTiposProibidos() {
        OrdemServico os = new OrdemServico();
        os.setOficinaId("of-1");
        os.setNumero("2026-0001");
        os.setRamo(Ramo.RADIADOR);
        ItemPeca peca = new ItemPeca();
        peca.setDescricao("Aditivo");
        peca.setQuantidade(new BigDecimal("2"));
        peca.setValorUnitario(new BigDecimal("39.90"));
        os.getItensPeca().add(peca);
        os.transicionarPara(StatusOS.APROVADA, "u1", "Joao", "ok");

        Map<String, Object> mapa = conversor.paraMapa(os);

        conferirTipos(mapa);
        assertInstanceOf(String.class, mapa.get("dataAbertura"), "Instant vira String ISO");
        assertEquals("APROVADA", mapa.get("status"), "enum vira o proprio nome");
        assertInstanceOf(Double.class, mapa.get("total"), "BigDecimal vira double");
    }

    @Test
    @DisplayName("round-trip preserva numero, datas, itens e historico (records)")
    void roundTrip() {
        OrdemServico original = new OrdemServico();
        original.setOficinaId("of-1");
        original.setNumero("2026-0042");
        original.setRamo(Ramo.RADIADOR);
        original.setKmEntrada(85000);
        original.setDataAbertura(Instant.parse("2026-07-16T12:00:00Z"));
        ItemPeca peca = new ItemPeca();
        peca.setDescricao("Colmeia");
        peca.setValorUnitario(new BigDecimal("320.00"));
        original.getItensPeca().add(peca);
        original.transicionarPara(StatusOS.APROVADA, "u1", "Joao", "cliente aprovou");

        OrdemServico volta = conversor.paraEntidade(conversor.paraMapa(original), OrdemServico.class);

        assertEquals(original.getNumero(), volta.getNumero());
        assertEquals(original.getDataAbertura(), volta.getDataAbertura());
        assertEquals(original.getKmEntrada(), volta.getKmEntrada());
        assertEquals(StatusOS.APROVADA, volta.getStatus());
        assertEquals(1, volta.getItensPeca().size());
        assertEquals(0, new BigDecimal("320.00").compareTo(volta.getItensPeca().get(0).getValorUnitario()));
        assertEquals(1, volta.getHistoricoStatus().size());
        assertEquals("Joao", volta.getHistoricoStatus().get(0).autorNome());
        assertEquals(0, original.getTotal().compareTo(volta.getTotal()));
    }

    @Test
    @DisplayName("round-trip da oficina preserva endereco (record) e config aninhada")
    void roundTripOficina() {
        Oficina original = new Oficina();
        original.setId("of-1");
        original.setNomeFantasia("Radiadores Silva");
        original.setEndereco(new Endereco("01310-100", "Av. Paulista", "1000", null,
                "Bela Vista", "Sao Paulo", "SP"));
        original.getConfig().setProximoNumeroOS(7);
        original.getConfig().setAliquotaServico(new BigDecimal("5.00"));

        Map<String, Object> mapa = conversor.paraMapa(original);
        conferirTipos(mapa);

        Oficina volta = conversor.paraEntidade(mapa, Oficina.class);
        assertEquals("Av. Paulista", volta.getEndereco().logradouro());
        assertEquals(7, volta.getConfig().getProximoNumeroOS());
        assertEquals(0, new BigDecimal("5.00").compareTo(volta.getConfig().getAliquotaServico()));
    }

    /** Firestore aceita: null, String, Boolean, Long/Integer/Double, Map e List desses. */
    @SuppressWarnings("unchecked")
    private void conferirTipos(Object valor) {
        if (valor == null || valor instanceof String || valor instanceof Boolean) {
            return;
        }
        if (valor instanceof BigDecimal || valor instanceof BigInteger || valor instanceof Instant) {
            throw new AssertionError("Tipo nao suportado pelo Firestore vazou: " + valor.getClass());
        }
        if (valor instanceof Number) {
            assertTrue(valor instanceof Long || valor instanceof Integer || valor instanceof Double,
                    "Numero de tipo inesperado: " + valor.getClass());
            return;
        }
        if (valor instanceof Map<?, ?> mapa) {
            ((Map<String, Object>) mapa).values().forEach(this::conferirTipos);
            return;
        }
        if (valor instanceof List<?> lista) {
            lista.forEach(this::conferirTipos);
            return;
        }
        throw new AssertionError("Valor nao primitivo no mapa: " + valor.getClass());
    }
}
