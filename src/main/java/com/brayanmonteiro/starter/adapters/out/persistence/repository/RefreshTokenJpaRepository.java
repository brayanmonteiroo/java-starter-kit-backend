package com.brayanmonteiro.starter.adapters.out.persistence.repository;

import com.brayanmonteiro.starter.adapters.out.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Set;
import org.springframework.data.jpa.repository.Modifying;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);
    @Modifying
    @Query("update RefreshTokenEntity r set r.revokedAt = CURRENT_TIMESTAMP where r.userId = :userId and r.revokedAt is null")
    void revokeAllForUser(@Param("userId") Long userId);
}
