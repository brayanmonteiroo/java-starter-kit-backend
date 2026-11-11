package com.brayanmonteiro.starter.adapters.out.persistence.adapter;

import com.brayanmonteiro.starter.adapters.out.persistence.entity.RoleEntity;
import com.brayanmonteiro.starter.adapters.out.persistence.repository.RoleJpaRepository;
import com.brayanmonteiro.starter.domain.access.Role;
import com.brayanmonteiro.starter.domain.access.RoleRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class RoleRepositoryAdapter implements RoleRepositoryPort {
    private final RoleJpaRepository repo;

    public RoleRepositoryAdapter(RoleJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Role save(Role role) {
        RoleEntity entity = role.getId() == null ? new RoleEntity() :
                repo.findById(role.getId()).orElseGet(RoleEntity::new);
        entity.setName(role.getName());
        entity.setCreatedAt(role.getCreatedAt());
        entity.setUpdatedAt(role.getUpdatedAt());
        entity.setPermissionIds(new HashSet<>(role.getPermissionIds()));
        entity = repo.save(entity);
        if (role.getId() == null) {
            role.assignId(entity.getId());
        }
        return toDomain(entity);
    }

    @Override
    public Optional<Role> findById(Long id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Role> findByName(String name) {
        return repo.findByName(name).map(this::toDomain);
    }

    @Override
    public List<Role> findAll() {
        return repo.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Role> findByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return repo.findAllById(ids).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByName(String name) {
        return repo.existsByName(name);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        return repo.existsByNameAndIdNot(name, id);
    }

    @Override
    public long count() {
        return repo.count();
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    private Role toDomain(RoleEntity e) {
        return new Role(e.getId(), e.getName(), e.getCreatedAt(), e.getUpdatedAt(), e.getPermissionIds());
    }
}
