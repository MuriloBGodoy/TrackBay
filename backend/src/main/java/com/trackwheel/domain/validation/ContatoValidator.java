package com.trackwheel.domain.validation;

import java.util.regex.Pattern;

/** Validacao de e-mail e telefone brasileiro (fixo 10 digitos / celular 11 digitos com DDD). */
public final class ContatoValidator {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s.]+(\\.[^@\\s.]+)+$");

    private ContatoValidator() {
    }

    public static boolean emailValido(String email) {
        return email != null && EMAIL.matcher(email.trim()).matches();
    }

    /** Aceita 10 (fixo) ou 11 (celular, iniciando por 9) digitos, com DDD valido. */
    public static boolean telefoneValido(String telefone) {
        String d = DocumentoValidator.somenteDigitos(telefone);
        if (d.length() != 10 && d.length() != 11) {
            return false;
        }
        int ddd = Integer.parseInt(d.substring(0, 2));
        if (ddd < 11 || ddd > 99) {
            return false;
        }
        if (d.length() == 11 && d.charAt(2) != '9') {
            return false;
        }
        return true;
    }

    public static String formatarTelefone(String telefone) {
        String d = DocumentoValidator.somenteDigitos(telefone);
        if (d.length() == 11) {
            return "(" + d.substring(0, 2) + ") " + d.substring(2, 7) + "-" + d.substring(7);
        }
        if (d.length() == 10) {
            return "(" + d.substring(0, 2) + ") " + d.substring(2, 6) + "-" + d.substring(6);
        }
        return telefone;
    }
}
