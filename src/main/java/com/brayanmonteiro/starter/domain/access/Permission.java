package com.brayanmonteiro.starter.domain.access;

import java.time.Instant;
import java.util.Objects;

public class Permission {
    private Long id;
    private final String name;
    private final Instant createdAt;

    public Permission(Long id, String name, Instant createdAt) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.createdAt = createdAt;
    }

    public static Permission create(String name, Instant now) {
        return new Permission(null, name, now);
    }

    public void assignId(Long id) { this.id = id; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public Instant getCreatedAt() { return createdAt; }
}
