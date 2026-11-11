package com.brayanmonteiro.starter.adapters.out.persistence.repository;

import com.brayanmonteiro.starter.adapters.out.persistence.entity.AuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditJpaRepository extends JpaRepository<AuditEntity, Long> {
    Page<AuditEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
