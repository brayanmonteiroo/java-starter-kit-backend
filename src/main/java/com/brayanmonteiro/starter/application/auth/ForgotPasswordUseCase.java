package com.brayanmonteiro.starter.application.auth;

import com.brayanmonteiro.starter.domain.identity.PasswordResetToken;
import com.brayanmonteiro.starter.domain.identity.PasswordResetTokenRepositoryPort;
import com.brayanmonteiro.starter.domain.identity.TokenHasherPort;
import com.brayanmonteiro.starter.domain.identity.UserRepositoryPort;
import com.brayanmonteiro.starter.domain.notification.DomainEventPublisherPort;
import com.brayanmonteiro.starter.domain.notification.PasswordResetRequestedEvent;
import com.brayanmonteiro.starter.domain.shared.ClockPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

@Service
public class ForgotPasswordUseCase {
    public static final String ROUTING_KEY = "mail.password-reset";

    private final UserRepositoryPort users;
    private final PasswordResetTokenRepositoryPort resetTokens;
    private final TokenHasherPort tokenHasher;
    private final DomainEventPublisherPort events;
    private final ClockPort clock;
    private final String frontendUrl;
    private final SecureRandom random = new SecureRandom();

    public ForgotPasswordUseCase(UserRepositoryPort users, PasswordResetTokenRepositoryPort resetTokens,
                                 TokenHasherPort tokenHasher, DomainEventPublisherPort events, ClockPort clock,
                                 @Value("${app.frontend-url}") String frontendUrl) {
        this.users = users;
        this.resetTokens = resetTokens;
        this.tokenHasher = tokenHasher;
        this.events = events;
        this.clock = clock;
        this.frontendUrl = frontendUrl;
    }

    @Transactional
    public void execute(String email) {
        users.findByEmail(email.toLowerCase()).ifPresent(user -> {
            var now = clock.now();
            String raw = generateToken();
            resetTokens.save(PasswordResetToken.issue(user.getId(), tokenHasher.hash(raw), now.plus(Duration.ofHours(1)), now));
            String url = frontendUrl.replaceAll("/$", "") + "/auth/reset-password?token=" + raw + "&email=" + user.getEmail();
            events.publish(ROUTING_KEY, new PasswordResetRequestedEvent(user.getEmail(), user.getName(), url));
        });
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
