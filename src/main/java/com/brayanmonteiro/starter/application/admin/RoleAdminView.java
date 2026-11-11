package com.brayanmonteiro.starter.application.admin;

import java.time.Instant;
import java.util.Set;

public record RoleAdminView(
        Long id,
        String name,
        Set<String> permissions,
        long userCount,
        Instant createdAt,
        Instant updatedAt
) {}
