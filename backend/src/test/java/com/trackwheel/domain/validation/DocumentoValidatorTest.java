package com.trackwheel.domain.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentoValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"529.982.247-25", "52998224725", "111.444.777-35", "11144477735"})
    @DisplayName("aceita CPF com digito verificador correto, com ou sem mascara")
    void cpfValido(String cpf) {
        assertTrue(DocumentoValidator.cpfValido(cpf));
    }

    @ParameterizedTest
    @ValueSource(strings = {"529.982.247-26", "12345678901", "111.111.111-11", "000.000.000-00", "5299822472", "abc"})
    @DisplayName("rejeita CPF com digito errado, tamanho errado ou digitos repetidos")
    void cpfInvalido(String cpf) {
        assertFalse(DocumentoValidator.cpfValido(cpf));
    }

    @Test
    @DisplayName("rejeita CPF nulo ou vazio")
    void cpfNuloOuVazio() {
        assertFalse(DocumentoValidator.cpfValido(null));
        assertFalse(DocumentoValidator.cpfValido(""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"11.222.333/0001-81", "11222333000181", "04.252.011/0001-10"})
    @DisplayName("aceita CNPJ com digito verificador correto")
    void cnpjValido(String cnpj) {
        assertTrue(DocumentoValidator.cnpjValido(cnpj));
    }

    @ParameterizedTest
    @ValueSource(strings = {"11.222.333/0001-82", "11111111111111", "1122233300018", "xyz"})
    @DisplayName("rejeita CNPJ invalido")
    void cnpjInvalido(String cnpj) {
        assertFalse(DocumentoValidator.cnpjValido(cnpj));
    }

    @Test
    @DisplayName("formata CPF e CNPJ para exibicao")
    void formatacao() {
        assertEquals("529.982.247-25", DocumentoValidator.formatarCpf("52998224725"));
        assertEquals("11.222.333/0001-81", DocumentoValidator.formatarCnpj("11222333000181"));
    }

    @Test
    @DisplayName("formatacao devolve a entrada quando o tamanho nao bate")
    void formatacaoTamanhoInvalido() {
        assertEquals("123", DocumentoValidator.formatarCpf("123"));
        assertEquals("123", DocumentoValidator.formatarCnpj("123"));
    }
}
