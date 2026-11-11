package com.brayanmonteiro.starter.adapters.out.persistence.adapter;

import com.brayanmonteiro.starter.adapters.out.persistence.entity.PasswordResetTokenEntity;
import com.brayanmonteiro.starter.adapters.out.persistence.repository.PasswordResetTokenJpaRepository;
import com.brayanmonteiro.starter.domain.identity.PasswordResetToken;
import com.brayanmonteiro.starter.domain.identity.PasswordResetTokenRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepositoryPort {
    private final PasswordResetTokenJpaRepository repo;

    public PasswordResetTokenRepositoryAdapter(PasswordResetTokenJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        PasswordResetTokenEntity entity = token.getId() == null ? new PasswordResetTokenEntity() :
                repo.findById(token.getId()).orElseGet(PasswordResetTokenEntity::new);
        entity.setUserId(token.getUserId());
        entity.setTokenHash(token.getTokenHash());
        entity.setExpiresAt(token.getExpiresAt());
        entity.setUsedAt(token.getUsedAt());
        entity.setCreatedAt(token.getCreatedAt());
        entity = repo.save(entity);
        if (token.getId() == null) {
            token.assignId(entity.getId());
        }
        return toDomain(entity);
    }

    @Override
    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return repo.findByTokenHash(tokenHash).map(this::toDomain);
    }

    private PasswordResetToken toDomain(PasswordResetTokenEntity e) {
        return new PasswordResetToken(e.getId(), e.getUserId(), e.getTokenHash(), e.getExpiresAt(), e.getUsedAt(), e.getCreatedAt());
    }
}
