package com.brayanmonteiro.starter.domain.audit;

import java.util.List;
import java.util.Optional;

public interface AuditRepositoryPort {
    AuditEntry save(AuditEntry entry);
    Optional<AuditEntry> findById(Long id);
    List<AuditEntry> findAll(int page, int size);
    long count();
}
