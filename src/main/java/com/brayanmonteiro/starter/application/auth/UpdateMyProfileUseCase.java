package com.brayanmonteiro.starter.application.auth;

import com.brayanmonteiro.starter.domain.identity.PasswordHasherPort;
import com.brayanmonteiro.starter.domain.identity.UserRepositoryPort;
import com.brayanmonteiro.starter.domain.shared.ClockPort;
import com.brayanmonteiro.starter.domain.shared.ConflictException;
import com.brayanmonteiro.starter.domain.shared.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateMyProfileUseCase {
    private final UserRepositoryPort users;
    private final PasswordHasherPort passwordHasher;
    private final ClockPort clock;
    private final GetCurrentUserUseCase getCurrentUserUseCase;

    public UpdateMyProfileUseCase(UserRepositoryPort users, PasswordHasherPort passwordHasher, ClockPort clock,
                                  GetCurrentUserUseCase getCurrentUserUseCase) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.clock = clock;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
    }

    @Transactional
    public AuthUserView execute(Long userId, String name, String email, String newPasswordOrNull) {
        var user = users.findById(userId).orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        if (users.existsByEmailAndIdNot(email.toLowerCase(), userId)) {
            throw new ConflictException("E-mail já cadastrado");
        }
        user.rename(name);
        user.changeEmail(email);
        if (newPasswordOrNull != null && !newPasswordOrNull.isBlank()) {
            user.changePasswordHash(passwordHasher.hash(newPasswordOrNull));
        }
        user.touch(clock.now());
        users.save(user);
        return getCurrentUserUseCase.execute(userId);
    }
}
