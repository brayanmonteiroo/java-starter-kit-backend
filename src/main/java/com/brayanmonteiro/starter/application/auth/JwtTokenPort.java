package com.brayanmonteiro.starter.application.auth;

import java.util.Set;

public interface JwtTokenPort {
    String createAccessToken(Long userId, String email, Set<String> roles, Set<String> permissions);
    long accessTokenTtlSeconds();
    AccessTokenClaims parseAccessToken(String token);

    record AccessTokenClaims(Long userId, String email, Set<String> roles, Set<String> permissions) {}
}
