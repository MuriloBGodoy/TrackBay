package com.trackwheel.domain.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlacaValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"ABC1234", "abc1234", "ABC-1234", "abc-1234", " ABC1234 "})
    @DisplayName("aceita placa no padrao antigo em qualquer grafia")
    void placaAntiga(String placa) {
        assertTrue(PlacaValidator.valida(placa));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ABC1D23", "abc1d23", "BRA2E19"})
    @DisplayName("aceita placa no padrao Mercosul")
    void placaMercosul(String placa) {
        assertTrue(PlacaValidator.valida(placa));
    }

    @ParameterizedTest
    @ValueSource(strings = {"AB1234", "ABCD123", "1234ABC", "ABC12345", "ABC12D3", "", "ABC"})
    @DisplayName("rejeita placa fora dos dois padroes")
    void placaInvalida(String placa) {
        assertFalse(PlacaValidator.valida(placa));
    }

    @Test
    @DisplayName("normaliza para maiusculo sem hifen — e assim que a placa e salva e buscada")
    void normalizacao() {
        assertEquals("ABC1234", PlacaValidator.normalizar("abc-1234"));
        assertEquals("ABC1D23", PlacaValidator.normalizar(" abc 1d23 "));
        assertEquals("", PlacaValidator.normalizar(null));
    }

    @Test
    @DisplayName("distingue Mercosul do padrao antigo")
    void deteccaoMercosul() {
        assertTrue(PlacaValidator.isMercosul("ABC1D23"));
        assertFalse(PlacaValidator.isMercosul("ABC1234"));
    }

    @Test
    @DisplayName("formata com hifen apenas no padrao antigo")
    void formatacao() {
        assertEquals("ABC-1234", PlacaValidator.formatar("abc1234"));
        assertEquals("ABC1D23", PlacaValidator.formatar("abc1d23"));
    }
}
