package com.brayanmonteiro.starter.application.admin;

import com.brayanmonteiro.starter.application.shared.AuditContext;
import com.brayanmonteiro.starter.domain.audit.AuditEntry;
import com.brayanmonteiro.starter.domain.audit.AuditRepositoryPort;
import com.brayanmonteiro.starter.domain.shared.ClockPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
    private final AuditRepositoryPort audits;
    private final ClockPort clock;

    public AuditService(AuditRepositoryPort audits, ClockPort clock) {
        this.audits = audits;
        this.clock = clock;
    }

    @Transactional
    public void record(AuditContext ctx, String event, String type, Long id, String oldJson, String newJson) {
        audits.save(new AuditEntry(
                null,
                ctx.actorUserId(),
                ctx.actorEmail(),
                event,
                type,
                id,
                oldJson,
                newJson,
                ctx.ipAddress(),
                ctx.userAgent(),
                clock.now()
        ));
    }
}
