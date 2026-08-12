package com.trackwheel.infrastructure.firestore;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converte entidade de dominio <-> mapa que o Firestore aceita, via Jackson.
 * Assim os modelos continuam sem anotacao de persistencia e os tipos que o
 * Firestore nao conhece viram tipos suportados:
 * Instant/LocalDate -> String ISO-8601 (ordenavel), BigDecimal -> double, enum -> nome.
 */
@Component
@Profile("!dev")
public class ConversorFirestore {

    private static final TypeReference<Map<String, Object>> MAPA = new TypeReference<>() {
    };

    private final ObjectMapper mapper;

    public ConversorFirestore() {
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public Map<String, Object> paraMapa(Object entidade) {
        return normalizarMapa(mapper.convertValue(entidade, MAPA));
    }

    public <T> T paraEntidade(Map<String, Object> dados, Class<T> tipo) {
        return mapper.convertValue(dados, tipo);
    }

    /** O Firestore so aceita double/long: BigDecimal e BigInteger precisam ser rebaixados. */
    @SuppressWarnings("unchecked")
    private Object normalizar(Object valor) {
        if (valor instanceof BigDecimal bd) {
            return bd.doubleValue();
        }
        if (valor instanceof BigInteger bi) {
            return bi.longValue();
        }
        if (valor instanceof Map<?, ?> mapa) {
            return normalizarMapa((Map<String, Object>) mapa);
        }
        if (valor instanceof List<?> lista) {
            return lista.stream().map(this::normalizar).toList();
        }
        return valor;
    }

    private Map<String, Object> normalizarMapa(Map<String, Object> mapa) {
        Map<String, Object> saida = new LinkedHashMap<>();
        mapa.forEach((chave, valor) -> saida.put(chave, normalizar(valor)));
        return saida;
    }
}
