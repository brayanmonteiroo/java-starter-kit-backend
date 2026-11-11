package com.brayanmonteiro.starter.domain.audit;

import java.time.Instant;

public class AuditEntry {
    private Long id;
    private final Long userId;
    private final String userEmail;
    private final String event;
    private final String auditableType;
    private final Long auditableId;
    private final String oldValuesJson;
    private final String newValuesJson;
    private final String ipAddress;
    private final String userAgent;
    private final Instant createdAt;

    public AuditEntry(Long id, Long userId, String userEmail, String event, String auditableType, Long auditableId,
                      String oldValuesJson, String newValuesJson, String ipAddress, String userAgent, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.event = event;
        this.auditableType = auditableType;
        this.auditableId = auditableId;
        this.oldValuesJson = oldValuesJson;
        this.newValuesJson = newValuesJson;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.createdAt = createdAt;
    }

    public void assignId(Long id) { this.id = id; }
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getUserEmail() { return userEmail; }
    public String getEvent() { return event; }
    public String getAuditableType() { return auditableType; }
    public Long getAuditableId() { return auditableId; }
    public String getOldValuesJson() { return oldValuesJson; }
    public String getNewValuesJson() { return newValuesJson; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public Instant getCreatedAt() { return createdAt; }
}
