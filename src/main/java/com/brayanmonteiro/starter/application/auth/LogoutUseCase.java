package com.brayanmonteiro.starter.application.auth;

import com.brayanmonteiro.starter.domain.identity.RefreshTokenRepositoryPort;
import com.brayanmonteiro.starter.domain.identity.TokenHasherPort;
import com.brayanmonteiro.starter.domain.shared.ClockPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogoutUseCase {
    private final RefreshTokenRepositoryPort refreshTokens;
    private final TokenHasherPort tokenHasher;
    private final ClockPort clock;

    public LogoutUseCase(RefreshTokenRepositoryPort refreshTokens, TokenHasherPort tokenHasher, ClockPort clock) {
        this.refreshTokens = refreshTokens;
        this.tokenHasher = tokenHasher;
        this.clock = clock;
    }

    @Transactional
    public void execute(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        refreshTokens.findByTokenHash(tokenHasher.hash(rawRefreshToken)).ifPresent(token -> {
            token.revoke(clock.now());
            refreshTokens.save(token);
        });
    }

    @Transactional
    public void revokeAll(Long userId) {
        refreshTokens.revokeAllForUser(userId);
    }
}
