package com.trackwheel.domain.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContatoValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"joao@oficina.com.br", "a@b.co"})
    void emailValido(String email) {
        assertTrue(ContatoValidator.emailValido(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"joao@", "@oficina.com", "joao oficina.com", "joao@oficina", ""})
    void emailInvalido(String email) {
        assertFalse(ContatoValidator.emailValido(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"(11) 98765-4321", "11987654321", "(11) 3456-7890", "1134567890"})
    @DisplayName("aceita celular com 9 e fixo com DDD valido")
    void telefoneValido(String telefone) {
        assertTrue(ContatoValidator.telefoneValido(telefone));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "987654321",     // sem DDD
            "11887654321",   // celular de 11 digitos sem o 9
            "0198765432",    // DDD invalido
            "119876543210",  // digitos demais
            ""
    })
    void telefoneInvalido(String telefone) {
        assertFalse(ContatoValidator.telefoneValido(telefone));
    }

    @Test
    void formatacao() {
        assertEquals("(11) 98765-4321", ContatoValidator.formatarTelefone("11987654321"));
        assertEquals("(11) 3456-7890", ContatoValidator.formatarTelefone("1134567890"));
    }
}
