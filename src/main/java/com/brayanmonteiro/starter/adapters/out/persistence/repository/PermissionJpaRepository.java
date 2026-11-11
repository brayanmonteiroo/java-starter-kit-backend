package com.brayanmonteiro.starter.adapters.out.persistence.repository;

import com.brayanmonteiro.starter.adapters.out.persistence.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Set;

public interface PermissionJpaRepository extends JpaRepository<PermissionEntity, Long> {
    Optional<PermissionEntity> findByName(String name);
    @Query(value = """
        SELECT DISTINCT p.name FROM permissions p
        INNER JOIN role_permissions rp ON rp.permission_id = p.id
        WHERE rp.role_id IN (:roleIds)
        """, nativeQuery = true)
    Set<String> findNamesByRoleIds(@Param("roleIds") Set<Long> roleIds);
}
