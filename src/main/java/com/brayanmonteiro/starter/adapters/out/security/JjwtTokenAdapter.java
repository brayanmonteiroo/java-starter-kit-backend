package com.brayanmonteiro.starter.adapters.out.security;

import com.brayanmonteiro.starter.application.auth.JwtTokenPort;
import com.brayanmonteiro.starter.domain.shared.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class JjwtTokenAdapter implements JwtTokenPort {
    private final SecretKey key;
    private final long accessMinutes;

    public JjwtTokenAdapter(@Value("${app.jwt.secret}") String secret,
                            @Value("${app.jwt.access-token-minutes}") long accessMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessMinutes = accessMinutes;
    }

    @Override
    public String createAccessToken(Long userId, String email, Set<String> roles, Set<String> permissions) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("roles", List.copyOf(roles))
                .claim("permissions", List.copyOf(permissions))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessMinutes * 60)))
                .signWith(key)
                .compact();
    }

    @Override
    public long accessTokenTtlSeconds() {
        return accessMinutes * 60;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AccessTokenClaims parseAccessToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            Long userId = Long.valueOf(claims.getSubject());
            String email = claims.get("email", String.class);
            Set<String> roles = toSet(claims.get("roles", Collection.class));
            Set<String> permissions = toSet(claims.get("permissions", Collection.class));
            return new AccessTokenClaims(userId, email, roles, permissions);
        } catch (Exception ex) {
            throw new UnauthorizedException("Token inválido");
        }
    }

    private Set<String> toSet(Collection<?> values) {
        if (values == null) {
            return Set.of();
        }
        Set<String> set = new LinkedHashSet<>();
        for (Object value : values) {
            set.add(String.valueOf(value));
        }
        return set;
    }
}
