package com.brayanmonteiro.starter.application.shared;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Serializa payloads de auditoria de forma segura (escapa aspas/controle),
 * evitando JSON quebrado e vetores de injeção no histórico.
 */
public final class AuditJson {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AuditJson() {}

    public static String of(String... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("Pares chave/valor incompletos");
        }
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        try {
            return MAPPER.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar auditoria", e);
        }
    }
}
