package com.brayanmonteiro.starter.application.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditJsonTest {

    @Test
    void escapesQuotesAndControlCharacters() {
        String json = AuditJson.of("name", "Ana \"XSS\" <script>", "email", "a@b.c");
        assertTrue(json.contains("\\\"XSS\\\"") || json.contains("\\u0022"));
        assertTrue(json.contains("<script>") || json.contains("\\u003c"));
        assertTrue(json.contains("a@b.c"));
    }

    @Test
    void serializesSimplePayload() {
        assertEquals("{\"email\":\"admin@test\",\"name\":\"Admin\"}", AuditJson.of("email", "admin@test", "name", "Admin"));
    }
}
