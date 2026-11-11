package com.brayanmonteiro.starter.domain.access;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RoleRepositoryPort {
    Role save(Role role);
    Optional<Role> findById(Long id);
    Optional<Role> findByName(String name);
    List<Role> findAll();
    List<Role> findByIds(Set<Long> ids);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
    long count();
    void deleteById(Long id);
}
