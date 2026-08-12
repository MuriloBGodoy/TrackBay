package com.trackwheel.domain.validation;

/** Validacao de digitos verificadores de CPF e CNPJ. Sem dependencia de framework. */
public final class DocumentoValidator {

    private DocumentoValidator() {
    }

    public static String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    public static boolean cpfValido(String cpf) {
        String d = somenteDigitos(cpf);
        if (d.length() != 11 || todosDigitosIguais(d)) {
            return false;
        }
        int dv1 = digitoCpf(d, 9);
        int dv2 = digitoCpf(d, 10);
        return dv1 == charToInt(d, 9) && dv2 == charToInt(d, 10);
    }

    public static boolean cnpjValido(String cnpj) {
        String d = somenteDigitos(cnpj);
        if (d.length() != 14 || todosDigitosIguais(d)) {
            return false;
        }
        int[] pesos1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesos2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int dv1 = digitoCnpj(d, pesos1);
        int dv2 = digitoCnpj(d, pesos2);
        return dv1 == charToInt(d, 12) && dv2 == charToInt(d, 13);
    }

    public static String formatarCpf(String cpf) {
        String d = somenteDigitos(cpf);
        if (d.length() != 11) {
            return cpf;
        }
        return d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9);
    }

    public static String formatarCnpj(String cnpj) {
        String d = somenteDigitos(cnpj);
        if (d.length() != 14) {
            return cnpj;
        }
        return d.substring(0, 2) + "." + d.substring(2, 5) + "." + d.substring(5, 8)
                + "/" + d.substring(8, 12) + "-" + d.substring(12);
    }

    private static int digitoCpf(String d, int ate) {
        int soma = 0;
        int peso = ate + 1;
        for (int i = 0; i < ate; i++) {
            soma += charToInt(d, i) * peso--;
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static int digitoCnpj(String d, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += charToInt(d, i) * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static boolean todosDigitosIguais(String d) {
        return d.chars().distinct().count() == 1;
    }

    private static int charToInt(String d, int i) {
        return d.charAt(i) - '0';
    }
}
