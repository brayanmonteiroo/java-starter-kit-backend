package com.brayanmonteiro.starter.application.admin;

import com.brayanmonteiro.starter.application.shared.AuditContext;
import com.brayanmonteiro.starter.application.shared.AuditJson;
import com.brayanmonteiro.starter.application.shared.PageResult;
import com.brayanmonteiro.starter.domain.access.RoleRepositoryPort;
import com.brayanmonteiro.starter.domain.identity.PasswordHasherPort;
import com.brayanmonteiro.starter.domain.identity.User;
import com.brayanmonteiro.starter.domain.identity.UserRepositoryPort;
import com.brayanmonteiro.starter.domain.shared.ClockPort;
import com.brayanmonteiro.starter.domain.shared.ConflictException;
import com.brayanmonteiro.starter.domain.shared.DomainException;
import com.brayanmonteiro.starter.domain.shared.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserAdminUseCases {
    private final UserRepositoryPort users;
    private final RoleRepositoryPort roles;
    private final PasswordHasherPort passwordHasher;
    private final ClockPort clock;
    private final AuditService auditService;

    public UserAdminUseCases(UserRepositoryPort users, RoleRepositoryPort roles, PasswordHasherPort passwordHasher,
                             ClockPort clock, AuditService auditService) {
        this.users = users;
        this.roles = roles;
        this.passwordHasher = passwordHasher;
        this.clock = clock;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResult<UserAdminView> list(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        var items = users.findAll(safePage, safeSize).stream().map(this::toView).toList();
        return new PageResult<>(items, users.count(), safePage, safeSize);
    }

    @Transactional(readOnly = true)
    public UserAdminView get(Long id) {
        return toView(users.findById(id).orElseThrow(() -> new NotFoundException("Usuário não encontrado")));
    }

    @Transactional
    public UserAdminView create(String name, String email, String password, Set<Long> roleIds, AuditContext ctx) {
        if (users.existsByEmail(email.toLowerCase())) {
            throw new ConflictException("E-mail já cadastrado");
        }
        validateRoles(roleIds);
        var now = clock.now();
        var user = User.create(name, email, passwordHasher.hash(password), now);
        user.syncRoles(roleIds == null ? Set.of() : roleIds);
        user = users.save(user);
        auditService.record(ctx, "created", "User", user.getId(), null,
                AuditJson.of("email", user.getEmail(), "name", user.getName()));
        return toView(user);
    }

    @Transactional
    public UserAdminView update(Long id, String name, String email, String password, Set<Long> roleIds, AuditContext ctx) {
        var user = users.findById(id).orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        if (users.existsByEmailAndIdNot(email.toLowerCase(), id)) {
            throw new ConflictException("E-mail já cadastrado");
        }
        validateRoles(roleIds);
        String old = AuditJson.of("email", user.getEmail(), "name", user.getName());
        user.rename(name);
        user.changeEmail(email);
        if (password != null && !password.isBlank()) {
            user.changePasswordHash(passwordHasher.hash(password));
        }
        user.syncRoles(roleIds == null ? Set.of() : roleIds);
        user.touch(clock.now());
        user = users.save(user);
        auditService.record(ctx, "updated", "User", user.getId(), old,
                AuditJson.of("email", user.getEmail(), "name", user.getName()));
        return toView(user);
    }

    @Transactional
    public void delete(Long id, AuditContext ctx) {
        var user = users.findById(id).orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        users.deleteById(id);
        auditService.record(ctx, "deleted", "User", id, AuditJson.of("email", user.getEmail()), null);
    }

    private void validateRoles(Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        var found = roles.findByIds(roleIds);
        if (found.size() != roleIds.size()) {
            throw new DomainException("INVALID_ROLES", "Uma ou mais roles são inválidas");
        }
    }

    private UserAdminView toView(User user) {
        var roleNames = roles.findByIds(user.getRoleIds()).stream()
                .map(r -> r.getName())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new UserAdminView(user.getId(), user.getName(), user.getEmail(), roleNames, user.getCreatedAt(), user.getUpdatedAt());
    }
}
