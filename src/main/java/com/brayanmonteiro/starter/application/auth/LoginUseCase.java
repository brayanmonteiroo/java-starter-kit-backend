package com.brayanmonteiro.starter.application.auth;

import com.brayanmonteiro.starter.domain.access.PermissionRepositoryPort;
import com.brayanmonteiro.starter.domain.access.RoleRepositoryPort;
import com.brayanmonteiro.starter.domain.identity.PasswordHasherPort;
import com.brayanmonteiro.starter.domain.identity.RefreshToken;
import com.brayanmonteiro.starter.domain.identity.RefreshTokenRepositoryPort;
import com.brayanmonteiro.starter.domain.identity.TokenHasherPort;
import com.brayanmonteiro.starter.domain.identity.User;
import com.brayanmonteiro.starter.domain.identity.UserRepositoryPort;
import com.brayanmonteiro.starter.domain.shared.ClockPort;
import com.brayanmonteiro.starter.domain.shared.UnauthorizedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LoginUseCase {
    private final UserRepositoryPort users;
    private final RoleRepositoryPort roles;
    private final PermissionRepositoryPort permissions;
    private final PasswordHasherPort passwordHasher;
    private final TokenHasherPort tokenHasher;
    private final RefreshTokenRepositoryPort refreshTokens;
    private final JwtTokenPort jwt;
    private final ClockPort clock;
    private final Duration refreshTtl;
    private final SecureRandom random = new SecureRandom();

    public LoginUseCase(UserRepositoryPort users, RoleRepositoryPort roles, PermissionRepositoryPort permissions,
                        PasswordHasherPort passwordHasher, TokenHasherPort tokenHasher,
                        RefreshTokenRepositoryPort refreshTokens, JwtTokenPort jwt, ClockPort clock,
                        @org.springframework.beans.factory.annotation.Value("${app.jwt.refresh-token-days:7}") long refreshDays) {
        this.users = users;
        this.roles = roles;
        this.permissions = permissions;
        this.passwordHasher = passwordHasher;
        this.tokenHasher = tokenHasher;
        this.refreshTokens = refreshTokens;
        this.jwt = jwt;
        this.clock = clock;
        this.refreshTtl = Duration.ofDays(refreshDays);
    }

    @Transactional
    public TokenPair execute(String email, String password) {
        User user = users.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Credenciais inválidas"));
        if (!passwordHasher.matches(password, user.getPasswordHash())) {
            throw new UnauthorizedException("Credenciais inválidas");
        }
        return issueTokens(user);
    }

    TokenPair issueTokens(User user) {
        var now = clock.now();
        var roleNames = roles.findByIds(user.getRoleIds()).stream()
                .map(r -> r.getName())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> permissionNames = permissions.findNamesByRoleIds(user.getRoleIds());
        String access = jwt.createAccessToken(user.getId(), user.getEmail(), roleNames, permissionNames);
        String rawRefresh = generateRefresh();
        refreshTokens.save(RefreshToken.issue(user.getId(), tokenHasher.hash(rawRefresh), now.plus(refreshTtl), now));
        return new TokenPair(access, rawRefresh, jwt.accessTokenTtlSeconds());
    }

    private String generateRefresh() {
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
