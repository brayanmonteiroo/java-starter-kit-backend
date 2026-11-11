package com.brayanmonteiro.starter.domain.access;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PermissionRepositoryPort {
    Permission save(Permission permission);
    Optional<Permission> findByName(String name);
    List<Permission> findAll();
    List<Permission> findByIds(Set<Long> ids);
    Set<String> findNamesByRoleIds(Set<Long> roleIds);
}
