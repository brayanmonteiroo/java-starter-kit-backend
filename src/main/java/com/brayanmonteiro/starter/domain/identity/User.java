package com.brayanmonteiro.starter.domain.identity;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class User {
    private Long id;
    private String name;
    private String email;
    private String passwordHash;
    private Instant emailVerifiedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private final Set<Long> roleIds = new LinkedHashSet<>();

    public User(Long id, String name, String email, String passwordHash,
                Instant emailVerifiedAt, Instant createdAt, Instant updatedAt, Set<Long> roleIds) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.email = Objects.requireNonNull(email).toLowerCase();
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.emailVerifiedAt = emailVerifiedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        if (roleIds != null) {
            this.roleIds.addAll(roleIds);
        }
    }

    public static User create(String name, String email, String passwordHash, Instant now) {
        return new User(null, name, email, passwordHash, now, now, now, Set.of());
    }

    /** Cria usuário com data explícita (útil para seeds/demos). */
    public static User createAt(String name, String email, String passwordHash, Instant createdAt) {
        return new User(null, name, email, passwordHash, createdAt, createdAt, createdAt, Set.of());
    }

    public void rename(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public void changeEmail(String email) {
        this.email = Objects.requireNonNull(email).toLowerCase();
    }

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = Objects.requireNonNull(passwordHash);
    }

    public void syncRoles(Set<Long> roles) {
        roleIds.clear();
        if (roles != null) {
            roleIds.addAll(roles);
        }
    }

    public void touch(Instant now) {
        this.updatedAt = now;
    }

    public void assignId(Long id) {
        if (this.id != null) {
            throw new IllegalStateException("User already has id");
        }
        this.id = id;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Instant getEmailVerifiedAt() { return emailVerifiedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Set<Long> getRoleIds() { return Collections.unmodifiableSet(roleIds); }
}
