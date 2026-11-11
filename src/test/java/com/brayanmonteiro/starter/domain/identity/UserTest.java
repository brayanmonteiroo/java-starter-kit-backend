package com.brayanmonteiro.starter.domain.identity;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    @Test
    void createNormalizesEmailAndStoresRoles() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        User user = User.create("Admin", "Admin@Example.COM", "hash", now);
        user.syncRoles(Set.of(1L, 2L));

        assertEquals("admin@example.com", user.getEmail());
        assertEquals("Admin", user.getName());
        assertTrue(user.getRoleIds().contains(1L));
        assertEquals(2, user.getRoleIds().size());
    }
}
