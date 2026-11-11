package com.brayanmonteiro.starter.application.auth;

import com.brayanmonteiro.starter.domain.identity.PasswordHasherPort;
import com.brayanmonteiro.starter.domain.identity.PasswordResetTokenRepositoryPort;
import com.brayanmonteiro.starter.domain.identity.RefreshTokenRepositoryPort;
import com.brayanmonteiro.starter.domain.identity.TokenHasherPort;
import com.brayanmonteiro.starter.domain.identity.UserRepositoryPort;
import com.brayanmonteiro.starter.domain.shared.ClockPort;
import com.brayanmonteiro.starter.domain.shared.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResetPasswordUseCase {
    private final UserRepositoryPort users;
    private final PasswordResetTokenRepositoryPort resetTokens;
    private final RefreshTokenRepositoryPort refreshTokens;
    private final TokenHasherPort tokenHasher;
    private final PasswordHasherPort passwordHasher;
    private final ClockPort clock;

    public ResetPasswordUseCase(UserRepositoryPort users, PasswordResetTokenRepositoryPort resetTokens,
                                RefreshTokenRepositoryPort refreshTokens, TokenHasherPort tokenHasher,
                                PasswordHasherPort passwordHasher, ClockPort clock) {
        this.users = users;
        this.resetTokens = resetTokens;
        this.refreshTokens = refreshTokens;
        this.tokenHasher = tokenHasher;
        this.passwordHasher = passwordHasher;
        this.clock = clock;
    }

    @Transactional
    public void execute(String email, String rawToken, String newPassword) {
        var now = clock.now();
        var token = resetTokens.findByTokenHash(tokenHasher.hash(rawToken))
                .orElseThrow(() -> new DomainException("INVALID_TOKEN", "Token de reset inválido"));
        if (!token.isUsable(now)) {
            throw new DomainException("INVALID_TOKEN", "Token de reset inválido ou expirado");
        }
        var user = users.findById(token.getUserId())
                .orElseThrow(() -> new DomainException("INVALID_TOKEN", "Token de reset inválido"));
        if (!user.getEmail().equalsIgnoreCase(email)) {
            throw new DomainException("INVALID_TOKEN", "Token de reset inválido");
        }
        user.changePasswordHash(passwordHasher.hash(newPassword));
        user.touch(now);
        users.save(user);
        token.markUsed(now);
        resetTokens.save(token);
        refreshTokens.revokeAllForUser(user.getId());
    }
}
