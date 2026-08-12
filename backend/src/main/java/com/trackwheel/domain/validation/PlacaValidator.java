package com.trackwheel.domain.validation;

import java.util.regex.Pattern;

/**
 * Validacao de placa nos dois formatos aceitos no Brasil.
 * A placa e a busca principal do app: sempre normalizada em maiusculo, sem hifen e sem espaco.
 */
public final class PlacaValidator {

    /** Padrao antigo: ABC1234 */
    private static final Pattern ANTIGA = Pattern.compile("^[A-Z]{3}\\d{4}$");
    /** Padrao Mercosul: ABC1D23 */
    private static final Pattern MERCOSUL = Pattern.compile("^[A-Z]{3}\\d[A-Z]\\d{2}$");

    private PlacaValidator() {
    }

    /** Remove hifen, espaco e pontuacao; sobe para maiusculo. */
    public static String normalizar(String placa) {
        if (placa == null) {
            return "";
        }
        return placa.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    public static boolean valida(String placa) {
        String p = normalizar(placa);
        return ANTIGA.matcher(p).matches() || MERCOSUL.matcher(p).matches();
    }

    public static boolean isMercosul(String placa) {
        return MERCOSUL.matcher(normalizar(placa)).matches();
    }

    /** Exibicao: ABC-1234 no padrao antigo; Mercosul nao usa hifen. */
    public static String formatar(String placa) {
        String p = normalizar(placa);
        if (ANTIGA.matcher(p).matches()) {
            return p.substring(0, 3) + "-" + p.substring(3);
        }
        return p;
    }
}
