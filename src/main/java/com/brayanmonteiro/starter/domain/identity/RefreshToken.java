package com.brayanmonteiro.starter.domain.identity;

import java.time.Instant;

public class RefreshToken {
    private Long id;
    private final Long userId;
    private final String tokenHash;
    private final Instant expiresAt;
    private Instant revokedAt;
    private final Instant createdAt;

    public RefreshToken(Long id, Long userId, String tokenHash, Instant expiresAt, Instant revokedAt, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.createdAt = createdAt;
    }

    public static RefreshToken issue(Long userId, String tokenHash, Instant expiresAt, Instant now) {
        return new RefreshToken(null, userId, tokenHash, expiresAt, null, now);
    }

    public void revoke(Instant now) {
        this.revokedAt = now;
    }

    public boolean isActive(Instant now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }

    public void assignId(Long id) { this.id = id; }
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getTokenHash() { return tokenHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
