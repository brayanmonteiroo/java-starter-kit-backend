package com.brayanmonteiro.starter.adapters.out.persistence.adapter;

import com.brayanmonteiro.starter.adapters.out.persistence.entity.RefreshTokenEntity;
import com.brayanmonteiro.starter.adapters.out.persistence.repository.RefreshTokenJpaRepository;
import com.brayanmonteiro.starter.domain.identity.RefreshToken;
import com.brayanmonteiro.starter.domain.identity.RefreshTokenRepositoryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {
    private final RefreshTokenJpaRepository repo;

    public RefreshTokenRepositoryAdapter(RefreshTokenJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        RefreshTokenEntity entity = token.getId() == null ? new RefreshTokenEntity() :
                repo.findById(token.getId()).orElseGet(RefreshTokenEntity::new);
        entity.setUserId(token.getUserId());
        entity.setTokenHash(token.getTokenHash());
        entity.setExpiresAt(token.getExpiresAt());
        entity.setRevokedAt(token.getRevokedAt());
        entity.setCreatedAt(token.getCreatedAt());
        entity = repo.save(entity);
        if (token.getId() == null) {
            token.assignId(entity.getId());
        }
        return toDomain(entity);
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return repo.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    @Transactional
    public void revokeAllForUser(Long userId) {
        repo.revokeAllForUser(userId);
    }

    private RefreshToken toDomain(RefreshTokenEntity e) {
        return new RefreshToken(e.getId(), e.getUserId(), e.getTokenHash(), e.getExpiresAt(), e.getRevokedAt(), e.getCreatedAt());
    }
}
