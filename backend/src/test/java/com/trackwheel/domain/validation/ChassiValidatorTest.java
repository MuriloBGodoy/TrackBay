package com.trackwheel.domain.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChassiValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"9BWZZZ377VT004251", "1HGBH41JXMN109186", "9bwzzz377vt004251"})
    @DisplayName("aceita VIN de 17 caracteres validos")
    void chassiValido(String chassi) {
        assertTrue(ChassiValidator.valido(chassi));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "9BWZZZ377VT00425",    // 16 caracteres
            "9BWZZZ377VT0042511",  // 18 caracteres
            "9BWIZZ377VT004251",   // contem I
            "9BWOZZ377VT004251",   // contem O
            "9BWQZZ377VT004251",   // contem Q
            ""
    })
    @DisplayName("rejeita VIN com tamanho errado ou letras proibidas (I, O, Q)")
    void chassiInvalido(String chassi) {
        assertFalse(ChassiValidator.valido(chassi));
    }

    @Test
    @DisplayName("normaliza para maiusculo removendo separadores")
    void normalizacao() {
        assertEquals("9BWZZZ377VT004251", ChassiValidator.normalizar("9bwzzz377-vt004251"));
        assertEquals("", ChassiValidator.normalizar(null));
    }
}
