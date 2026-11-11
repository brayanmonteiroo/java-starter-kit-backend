package com.brayanmonteiro.starter.application.admin;

import com.brayanmonteiro.starter.application.shared.AuditContext;
import com.brayanmonteiro.starter.application.shared.AuditJson;
import com.brayanmonteiro.starter.domain.access.PermissionNames;
import com.brayanmonteiro.starter.domain.access.PermissionRepositoryPort;
import com.brayanmonteiro.starter.domain.access.Role;
import com.brayanmonteiro.starter.domain.access.RoleRepositoryPort;
import com.brayanmonteiro.starter.domain.identity.UserRepositoryPort;
import com.brayanmonteiro.starter.domain.shared.ClockPort;
import com.brayanmonteiro.starter.domain.shared.ConflictException;
import com.brayanmonteiro.starter.domain.shared.DomainException;
import com.brayanmonteiro.starter.domain.shared.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleAdminUseCases {
    private final RoleRepositoryPort roles;
    private final PermissionRepositoryPort permissions;
    private final UserRepositoryPort users;
    private final ClockPort clock;
    private final AuditService auditService;

    public RoleAdminUseCases(RoleRepositoryPort roles, PermissionRepositoryPort permissions, UserRepositoryPort users,
                             ClockPort clock, AuditService auditService) {
        this.roles = roles;
        this.permissions = permissions;
        this.users = users;
        this.clock = clock;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<RoleAdminView> list() {
        Map<Long, Long> userCounts = countUsersByRole();
        return roles.findAll().stream().map(role -> toView(role, userCounts)).toList();
    }

    @Transactional(readOnly = true)
    public RoleAdminView get(Long id) {
        var role = roles.findById(id).orElseThrow(() -> new NotFoundException("Role não encontrada"));
        return toView(role, countUsersByRole());
    }

    @Transactional(readOnly = true)
    public PermissionCatalogView catalog() {
        var items = permissions.findAll().stream()
                .map(p -> new PermissionCatalogView.PermissionItem(p.getId(), p.getName()))
                .toList();
        return new PermissionCatalogView(items);
    }

    @Transactional
    public RoleAdminView create(String name, Set<Long> permissionIds, AuditContext ctx) {
        if (roles.existsByName(name)) {
            throw new ConflictException("Role já existe");
        }
        validatePermissions(permissionIds);
        var now = clock.now();
        var role = Role.create(name, now);
        role.syncPermissions(permissionIds == null ? Set.of() : permissionIds);
        role = roles.save(role);
        auditService.record(ctx, "created", "Role", role.getId(), null, AuditJson.of("name", role.getName()));
        return toView(role, countUsersByRole());
    }

    @Transactional
    public RoleAdminView update(Long id, String name, Set<Long> permissionIds, AuditContext ctx) {
        var role = roles.findById(id).orElseThrow(() -> new NotFoundException("Role não encontrada"));
        if (roles.existsByNameAndIdNot(name, id)) {
            throw new ConflictException("Role já existe");
        }
        validatePermissions(permissionIds);
        String old = AuditJson.of("name", role.getName());
        role.rename(name);
        role.syncPermissions(permissionIds == null ? Set.of() : permissionIds);
        role.touch(clock.now());
        role = roles.save(role);
        auditService.record(ctx, "updated", "Role", role.getId(), old, AuditJson.of("name", role.getName()));
        return toView(role, countUsersByRole());
    }

    @Transactional
    public void delete(Long id, AuditContext ctx) {
        var role = roles.findById(id).orElseThrow(() -> new NotFoundException("Role não encontrada"));
        if (PermissionNames.ROLE_ADMIN.equals(role.getName())) {
            throw new DomainException("PROTECTED_ROLE", "A role admin não pode ser removida");
        }
        roles.deleteById(id);
        auditService.record(ctx, "deleted", "Role", id, AuditJson.of("name", role.getName()), null);
    }

    private void validatePermissions(Set<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }
        var found = permissions.findByIds(permissionIds);
        if (found.size() != permissionIds.size()) {
            throw new DomainException("INVALID_PERMISSIONS", "Uma ou mais permissões são inválidas");
        }
    }

    private Map<Long, Long> countUsersByRole() {
        Map<Long, Long> counts = new HashMap<>();
        for (var user : users.findAll()) {
            for (Long roleId : user.getRoleIds()) {
                counts.merge(roleId, 1L, Long::sum);
            }
        }
        return counts;
    }

    private RoleAdminView toView(Role role, Map<Long, Long> userCounts) {
        var names = permissions.findByIds(role.getPermissionIds()).stream()
                .map(p -> p.getName())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        long userCount = userCounts.getOrDefault(role.getId(), 0L);
        return new RoleAdminView(role.getId(), role.getName(), names, userCount, role.getCreatedAt(), role.getUpdatedAt());
    }
}
