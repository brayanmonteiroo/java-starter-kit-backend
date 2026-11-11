package com.brayanmonteiro.starter.application.shared;

public record AuditContext(Long actorUserId, String actorEmail, String ipAddress, String userAgent) {
    public static AuditContext anonymous(String ip, String ua) {
        return new AuditContext(null, null, ip, ua);
    }
}
