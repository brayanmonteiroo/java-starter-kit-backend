package com.brayanmonteiro.starter.bootstrap;

import com.brayanmonteiro.starter.domain.access.Permission;
import com.brayanmonteiro.starter.domain.access.PermissionNames;
import com.brayanmonteiro.starter.domain.access.PermissionRepositoryPort;
import com.brayanmonteiro.starter.domain.access.Role;
import com.brayanmonteiro.starter.domain.access.RoleRepositoryPort;
import com.brayanmonteiro.starter.domain.identity.PasswordHasherPort;
import com.brayanmonteiro.starter.domain.identity.User;
import com.brayanmonteiro.starter.domain.identity.UserRepositoryPort;
import com.brayanmonteiro.starter.domain.shared.ClockPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DataSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final int DEMO_USER_TARGET = 35;

    private final boolean enabled;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminName;
    private final PermissionRepositoryPort permissions;
    private final RoleRepositoryPort roles;
    private final UserRepositoryPort users;
    private final PasswordHasherPort passwordHasher;
    private final ClockPort clock;

    public DataSeeder(@Value("${app.seed.enabled}") boolean enabled,
                      @Value("${app.seed.admin-email}") String adminEmail,
                      @Value("${app.seed.admin-password}") String adminPassword,
                      @Value("${app.seed.admin-name}") String adminName,
                      PermissionRepositoryPort permissions, RoleRepositoryPort roles, UserRepositoryPort users,
                      PasswordHasherPort passwordHasher, ClockPort clock) {
        this.enabled = enabled;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminName = adminName;
        this.permissions = permissions;
        this.roles = roles;
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        var now = clock.now();
        for (String name : PermissionNames.all()) {
            if (permissions.findByName(name).isEmpty()) {
                permissions.save(Permission.create(name, now));
            }
        }
        var allPermissionIds = permissions.findAll().stream().map(Permission::getId).collect(Collectors.toCollection(LinkedHashSet::new));
        var userPermissionIds = permissions.findAll().stream()
                .filter(p -> PermissionNames.userDefaults().contains(p.getName()))
                .map(Permission::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Role adminRole = roles.findByName(PermissionNames.ROLE_ADMIN).orElseGet(() -> Role.create(PermissionNames.ROLE_ADMIN, now));
        adminRole.syncPermissions(allPermissionIds);
        adminRole.touch(now);
        adminRole = roles.save(adminRole);

        Role userRole = roles.findByName(PermissionNames.ROLE_USER).orElseGet(() -> Role.create(PermissionNames.ROLE_USER, now));
        userRole.syncPermissions(userPermissionIds);
        userRole.touch(now);
        userRole = roles.save(userRole);

        if (users.findByEmail(adminEmail).isEmpty()) {
            User admin = User.create(adminName, adminEmail, passwordHasher.hash(adminPassword), now);
            admin.syncRoles(Set.of(adminRole.getId()));
            users.save(admin);
            log.info("Usuário admin seed criado: {}", adminEmail);
        }

        seedDemoUsers(userRole.getId(), now);
    }

    private void seedDemoUsers(Long userRoleId, Instant now) {
        if (users.count() >= DEMO_USER_TARGET) {
            return;
        }
        String hash = passwordHasher.hash("password");
        List<String> firstNames = List.of(
                "Ana", "Bruno", "Carla", "Diego", "Elena", "Felipe", "Gabi", "Hugo",
                "Iris", "João", "Karen", "Leo", "Marina", "Nico", "Olívia", "Paulo",
                "Queila", "Rafa", "Sofia", "Tiago", "Úrsula", "Vitor", "Wendy", "Yuri", "Zeca"
        );
        int created = 0;
        for (int i = 1; users.count() < DEMO_USER_TARGET; i++) {
            String email = "demo%02d@java-starter.test".formatted(i);
            if (users.existsByEmail(email)) {
                continue;
            }
            String name = firstNames.get((i - 1) % firstNames.size()) + " Demo " + i;
            // Espalha criação nos últimos 30 dias (para o gráfico 7/14/30 do painel)
            Instant createdAt = now.minus(Duration.ofDays((i - 1) % 30)).minus(Duration.ofHours(i % 20));
            User demo = User.createAt(name, email, hash, createdAt);
            demo.syncRoles(Set.of(userRoleId));
            users.save(demo);
            created++;
        }
        if (created > 0) {
            log.info("Seed demo: {} usuários criados (total atual {})", created, users.count());
        }
    }
}
