package com.brayanmonteiro.starter.adapters.out.persistence.adapter;

import com.brayanmonteiro.starter.adapters.out.persistence.entity.PermissionEntity;
import com.brayanmonteiro.starter.adapters.out.persistence.repository.PermissionJpaRepository;
import com.brayanmonteiro.starter.domain.access.Permission;
import com.brayanmonteiro.starter.domain.access.PermissionRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class PermissionRepositoryAdapter implements PermissionRepositoryPort {
    private final PermissionJpaRepository repo;

    public PermissionRepositoryAdapter(PermissionJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Permission save(Permission permission) {
        PermissionEntity entity = permission.getId() == null ? new PermissionEntity() :
                repo.findById(permission.getId()).orElseGet(PermissionEntity::new);
        entity.setName(permission.getName());
        entity.setCreatedAt(permission.getCreatedAt());
        entity = repo.save(entity);
        if (permission.getId() == null) {
            permission.assignId(entity.getId());
        }
        return toDomain(entity);
    }

    @Override
    public Optional<Permission> findByName(String name) {
        return repo.findByName(name).map(this::toDomain);
    }

    @Override
    public List<Permission> findAll() {
        return repo.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Permission> findByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return repo.findAllById(ids).stream().map(this::toDomain).toList();
    }

    @Override
    public Set<String> findNamesByRoleIds(Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Set.of();
        }
        return repo.findNamesByRoleIds(roleIds);
    }

    private Permission toDomain(PermissionEntity e) {
        return new Permission(e.getId(), e.getName(), e.getCreatedAt());
    }
}
