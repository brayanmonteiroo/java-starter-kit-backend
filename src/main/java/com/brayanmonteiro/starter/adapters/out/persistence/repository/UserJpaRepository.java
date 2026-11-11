package com.brayanmonteiro.starter.adapters.out.persistence.repository;

import com.brayanmonteiro.starter.adapters.out.persistence.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    Page<UserEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<UserEntity> findByCreatedAtGreaterThanEqualOrderByCreatedAtAsc(Instant from);
}
