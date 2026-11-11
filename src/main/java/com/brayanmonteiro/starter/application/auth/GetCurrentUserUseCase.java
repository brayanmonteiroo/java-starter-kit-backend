package com.brayanmonteiro.starter.application.auth;

import com.brayanmonteiro.starter.domain.access.PermissionRepositoryPort;
import com.brayanmonteiro.starter.domain.access.RoleRepositoryPort;
import com.brayanmonteiro.starter.domain.identity.UserRepositoryPort;
import com.brayanmonteiro.starter.domain.shared.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@Service
public class GetCurrentUserUseCase {
    private final UserRepositoryPort users;
    private final RoleRepositoryPort roles;
    private final PermissionRepositoryPort permissions;

    public GetCurrentUserUseCase(UserRepositoryPort users, RoleRepositoryPort roles, PermissionRepositoryPort permissions) {
        this.users = users;
        this.roles = roles;
        this.permissions = permissions;
    }

    @Transactional(readOnly = true)
    public AuthUserView execute(Long userId) {
        var user = users.findById(userId).orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        var roleNames = roles.findByIds(user.getRoleIds()).stream()
                .map(r -> r.getName())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        var permissionNames = permissions.findNamesByRoleIds(user.getRoleIds());
        return new AuthUserView(user.getId(), user.getName(), user.getEmail(), roleNames, permissionNames);
    }
}
