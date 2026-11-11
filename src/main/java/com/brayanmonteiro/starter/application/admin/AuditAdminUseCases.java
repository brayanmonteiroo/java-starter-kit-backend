package com.brayanmonteiro.starter.application.admin;

import com.brayanmonteiro.starter.application.shared.PageResult;
import com.brayanmonteiro.starter.domain.audit.AuditRepositoryPort;
import com.brayanmonteiro.starter.domain.shared.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditAdminUseCases {
    private final AuditRepositoryPort audits;

    public AuditAdminUseCases(AuditRepositoryPort audits) {
        this.audits = audits;
    }

    @Transactional(readOnly = true)
    public PageResult<AuditAdminView> list(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        var items = audits.findAll(safePage, safeSize).stream().map(a -> new AuditAdminView(
                a.getId(), a.getUserId(), a.getUserEmail(), a.getEvent(), a.getAuditableType(), a.getAuditableId(),
                a.getOldValuesJson(), a.getNewValuesJson(), a.getIpAddress(), a.getUserAgent(), a.getCreatedAt()
        )).toList();
        return new PageResult<>(items, audits.count(), safePage, safeSize);
    }

    @Transactional(readOnly = true)
    public AuditAdminView get(Long id) {
        var a = audits.findById(id).orElseThrow(() -> new NotFoundException("Auditoria não encontrada"));
        return new AuditAdminView(a.getId(), a.getUserId(), a.getUserEmail(), a.getEvent(), a.getAuditableType(),
                a.getAuditableId(), a.getOldValuesJson(), a.getNewValuesJson(), a.getIpAddress(), a.getUserAgent(), a.getCreatedAt());
    }
}
