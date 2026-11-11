package com.brayanmonteiro.starter.domain.access;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class Role {
    private Long id;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;
    private final Set<Long> permissionIds = new LinkedHashSet<>();

    public Role(Long id, String name, Instant createdAt, Instant updatedAt, Set<Long> permissionIds) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        if (permissionIds != null) {
            this.permissionIds.addAll(permissionIds);
        }
    }

    public static Role create(String name, Instant now) {
        return new Role(null, name, now, now, Set.of());
    }

    public void rename(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public void syncPermissions(Set<Long> permissions) {
        permissionIds.clear();
        if (permissions != null) {
            permissionIds.addAll(permissions);
        }
    }

    public void touch(Instant now) {
        this.updatedAt = now;
    }

    public void assignId(Long id) {
        if (this.id != null) {
            throw new IllegalStateException("Role already has id");
        }
        this.id = id;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Set<Long> getPermissionIds() { return Collections.unmodifiableSet(permissionIds); }
}
