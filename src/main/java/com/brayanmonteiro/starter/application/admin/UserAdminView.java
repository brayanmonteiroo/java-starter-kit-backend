package com.brayanmonteiro.starter.application.admin;

import java.time.Instant;
import java.util.Set;

public record UserAdminView(Long id, String name, String email, Set<String> roles, Instant createdAt, Instant updatedAt) {}
