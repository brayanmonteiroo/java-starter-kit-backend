package com.brayanmonteiro.starter.application.auth;

import com.brayanmonteiro.starter.domain.identity.RefreshTokenRepositoryPort;
import com.brayanmonteiro.starter.domain.identity.TokenHasherPort;
import com.brayanmonteiro.starter.domain.identity.UserRepositoryPort;
import com.brayanmonteiro.starter.domain.shared.ClockPort;
import com.brayanmonteiro.starter.domain.shared.UnauthorizedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenUseCase {
    private final RefreshTokenRepositoryPort refreshTokens;
    private final TokenHasherPort tokenHasher;
    private final UserRepositoryPort users;
    private final LoginUseCase loginUseCase;
    private final ClockPort clock;

    public RefreshTokenUseCase(RefreshTokenRepositoryPort refreshTokens, TokenHasherPort tokenHasher,
                               UserRepositoryPort users, LoginUseCase loginUseCase, ClockPort clock) {
        this.refreshTokens = refreshTokens;
        this.tokenHasher = tokenHasher;
        this.users = users;
        this.loginUseCase = loginUseCase;
        this.clock = clock;
    }

    @Transactional
    public TokenPair execute(String rawRefreshToken) {
        var now = clock.now();
        var stored = refreshTokens.findByTokenHash(tokenHasher.hash(rawRefreshToken))
                .orElseThrow(() -> new UnauthorizedException("Refresh token inválido"));
        if (!stored.isActive(now)) {
            throw new UnauthorizedException("Refresh token inválido ou expirado");
        }
        stored.revoke(now);
        refreshTokens.save(stored);
        var user = users.findById(stored.getUserId())
                .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));
        return loginUseCase.issueTokens(user);
    }
}
