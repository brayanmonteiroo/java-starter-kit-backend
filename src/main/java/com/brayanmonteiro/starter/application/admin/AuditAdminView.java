package com.brayanmonteiro.starter.application.admin;

import java.time.Instant;

public record AuditAdminView(Long id, Long userId, String userEmail, String event, String auditableType,
                             Long auditableId, String oldValuesJson, String newValuesJson,
                             String ipAddress, String userAgent, Instant createdAt) {}
