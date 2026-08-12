package com.trackwheel.domain.validation;

import java.util.regex.Pattern;

/** Validacao de chassi (VIN, ISO 3779): 17 caracteres, sem as letras I, O e Q. */
public final class ChassiValidator {

    private static final Pattern VIN = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$");

    private ChassiValidator() {
    }

    public static String normalizar(String chassi) {
        if (chassi == null) {
            return "";
        }
        return chassi.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    public static boolean valido(String chassi) {
        return VIN.matcher(normalizar(chassi)).matches();
    }
}
