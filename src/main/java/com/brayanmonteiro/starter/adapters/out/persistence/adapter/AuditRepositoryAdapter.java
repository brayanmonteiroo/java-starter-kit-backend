package com.brayanmonteiro.starter.adapters.out.persistence.adapter;

import com.brayanmonteiro.starter.adapters.out.persistence.entity.AuditEntity;
import com.brayanmonteiro.starter.adapters.out.persistence.repository.AuditJpaRepository;
import com.brayanmonteiro.starter.domain.audit.AuditEntry;
import com.brayanmonteiro.starter.domain.audit.AuditRepositoryPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AuditRepositoryAdapter implements AuditRepositoryPort {
    private final AuditJpaRepository repo;

    public AuditRepositoryAdapter(AuditJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public AuditEntry save(AuditEntry entry) {
        AuditEntity entity = new AuditEntity();
        entity.setUserId(entry.getUserId());
        entity.setUserEmail(entry.getUserEmail());
        entity.setEvent(entry.getEvent());
        entity.setAuditableType(entry.getAuditableType());
        entity.setAuditableId(entry.getAuditableId());
        entity.setOldValues(entry.getOldValuesJson());
        entity.setNewValues(entry.getNewValuesJson());
        entity.setIpAddress(entry.getIpAddress());
        entity.setUserAgent(entry.getUserAgent());
        entity.setCreatedAt(entry.getCreatedAt());
        entity = repo.save(entity);
        entry.assignId(entity.getId());
        return toDomain(entity);
    }

    @Override
    public Optional<AuditEntry> findById(Long id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<AuditEntry> findAll(int page, int size) {
        return repo.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size)).stream().map(this::toDomain).toList();
    }

    @Override
    public long count() {
        return repo.count();
    }

    private AuditEntry toDomain(AuditEntity e) {
        return new AuditEntry(e.getId(), e.getUserId(), e.getUserEmail(), e.getEvent(), e.getAuditableType(),
                e.getAuditableId(), e.getOldValues(), e.getNewValues(), e.getIpAddress(), e.getUserAgent(), e.getCreatedAt());
    }
}
