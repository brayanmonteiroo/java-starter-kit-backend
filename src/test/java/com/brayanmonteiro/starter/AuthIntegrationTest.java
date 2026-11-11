package com.brayanmonteiro.starter;

import com.brayanmonteiro.starter.domain.identity.PasswordResetToken;
import com.brayanmonteiro.starter.domain.identity.PasswordResetTokenRepositoryPort;
import com.brayanmonteiro.starter.domain.identity.TokenHasherPort;
import com.brayanmonteiro.starter.domain.identity.UserRepositoryPort;
import com.brayanmonteiro.starter.domain.shared.ClockPort;
import com.brayanmonteiro.starter.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthIntegrationTest extends AbstractIntegrationTest {

    private static final String XSS_EMAIL = "<script>alert(1)</script>@evil.test";
    private static final String XSS_PAYLOAD = "<img src=x onerror=alert('xss')>";

    @Autowired
    UserRepositoryPort users;

    @Autowired
    PasswordResetTokenRepositoryPort resetTokens;

    @Autowired
    TokenHasherPort tokenHasher;

    @Autowired
    ClockPort clock;

    @Test
    void healthAndLoginFlow() {
        ResponseEntity<Map> health = rest.getForEntity("/api/health", Map.class);
        assertEquals(HttpStatus.OK, health.getStatusCode());
        assertEquals("ok", health.getBody().get("status"));
        assertTrue(isJson(health));

        Map<String, Object> tokens = loginAsAdmin();
        assertNotNull(tokens.get("accessToken"));
        assertNotNull(tokens.get("refreshToken"));
        assertEquals("Bearer", tokens.get("tokenType"));

        ResponseEntity<Map> me = get("/api/user", bearerHeaders(String.valueOf(tokens.get("accessToken"))), Map.class);
        assertEquals(HttpStatus.OK, me.getStatusCode());
        assertTrue(isJson(me));
        assertEquals(ADMIN_EMAIL, me.getBody().get("email"));
        assertTrue(((Collection<?>) me.getBody().get("roles")).contains("admin"));
    }

    @Test
    void loginRejectsInvalidCredentialsWithGenericMessage() {
        ResponseEntity<Map> badPassword = post(
                "/api/login",
                Map.of("email", ADMIN_EMAIL, "password", "wrong-password"),
                jsonHeaders(),
                Map.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, badPassword.getStatusCode());
        assertTrue(isJson(badPassword));
        assertEquals("Credenciais inválidas", badPassword.getBody().get("detail"));

        ResponseEntity<Map> unknownUser = post(
                "/api/login",
                Map.of("email", "nobody@java-starter.test", "password", "password"),
                jsonHeaders(),
                Map.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, unknownUser.getStatusCode());
        assertEquals("Credenciais inválidas", unknownUser.getBody().get("detail"));
    }

    @Test
    void loginRejectsXssPayloadInEmailWithoutReflectingHtml() {
        ResponseEntity<String> response = post(
                "/api/login",
                Map.of("email", XSS_EMAIL, "password", "password"),
                jsonHeaders(),
                String.class
        );

        assertEquals(UNPROCESSABLE, response.getStatusCode());
        assertTrue(isJson(response));
        String body = response.getBody() == null ? "" : response.getBody();
        assertFalse(body.toLowerCase().contains("<script"));
        assertFalse(body.toLowerCase().contains("<html"));
        assertTrue(body.contains("VALIDATION_ERROR") || body.contains("errors"));
    }

    @Test
    void protectedRoutesRequireBearerAndReturnJsonProblem() {
        ResponseEntity<String> anonymous = get("/api/user", jsonHeaders(), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, anonymous.getStatusCode());
        assertTrue(isJson(anonymous));
        assertFalse(anonymous.getBody().toLowerCase().contains("<html"));

        ResponseEntity<String> garbage = get("/api/user", bearerHeaders("not.a.jwt"), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, garbage.getStatusCode());
        assertTrue(isJson(garbage));
    }

    @Test
    void refreshRotatesTokenAndLogoutRevokesRefresh() {
        Map<String, Object> login = loginAsAdmin();
        String refresh = String.valueOf(login.get("refreshToken"));

        ResponseEntity<Map> refreshed = post(
                "/api/auth/refresh",
                Map.of("refreshToken", refresh),
                jsonHeaders(),
                Map.class
        );
        assertEquals(HttpStatus.OK, refreshed.getStatusCode());
        String newRefresh = String.valueOf(refreshed.getBody().get("refreshToken"));
        assertNotEquals(refresh, newRefresh);

        ResponseEntity<Map> reuseOld = post(
                "/api/auth/refresh",
                Map.of("refreshToken", refresh),
                jsonHeaders(),
                Map.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, reuseOld.getStatusCode());

        ResponseEntity<Void> logout = post(
                "/api/logout",
                Map.of("refreshToken", newRefresh),
                jsonHeaders(),
                Void.class
        );
        assertEquals(HttpStatus.NO_CONTENT, logout.getStatusCode());

        ResponseEntity<Map> afterLogout = post(
                "/api/auth/refresh",
                Map.of("refreshToken", newRefresh),
                jsonHeaders(),
                Map.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, afterLogout.getStatusCode());
    }

    @Test
    void forgotPasswordDoesNotEnumerateUsersAndResetWorks() {
        ResponseEntity<Void> unknown = post(
                "/api/forgot-password",
                Map.of("email", "ghost@java-starter.test"),
                jsonHeaders(),
                Void.class
        );
        assertEquals(HttpStatus.ACCEPTED, unknown.getStatusCode());

        ResponseEntity<Void> known = post(
                "/api/forgot-password",
                Map.of("email", ADMIN_EMAIL),
                jsonHeaders(),
                Void.class
        );
        assertEquals(HttpStatus.ACCEPTED, known.getStatusCode());

        String rawToken = issueResetTokenFor(ADMIN_EMAIL);
        String newPassword = "password-reset-1";

        ResponseEntity<Void> reset = post(
                "/api/reset-password",
                Map.of("email", ADMIN_EMAIL, "token", rawToken, "password", newPassword),
                jsonHeaders(),
                Void.class
        );
        assertEquals(HttpStatus.NO_CONTENT, reset.getStatusCode());

        ResponseEntity<Map> oldLogin = post(
                "/api/login",
                Map.of("email", ADMIN_EMAIL, "password", ADMIN_PASSWORD),
                jsonHeaders(),
                Map.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, oldLogin.getStatusCode());

        Map<String, Object> newLogin = login(ADMIN_EMAIL, newPassword);
        assertNotNull(newLogin.get("accessToken"));

        // restaura senha seed para demais testes da suíte
        String restoreToken = issueResetTokenFor(ADMIN_EMAIL);
        ResponseEntity<Void> restore = post(
                "/api/reset-password",
                Map.of("email", ADMIN_EMAIL, "token", restoreToken, "password", ADMIN_PASSWORD),
                jsonHeaders(),
                Void.class
        );
        assertEquals(HttpStatus.NO_CONTENT, restore.getStatusCode());
    }

    @Test
    void resetPasswordRejectsShortPasswordAndInvalidTokenWithoutHtml() {
        ResponseEntity<String> shortPassword = post(
                "/api/reset-password",
                Map.of("email", ADMIN_EMAIL, "token", "abc", "password", "short"),
                jsonHeaders(),
                String.class
        );
        assertEquals(UNPROCESSABLE, shortPassword.getStatusCode());
        assertTrue(isJson(shortPassword));
        assertFalse(shortPassword.getBody().toLowerCase().contains("<script"));

        ResponseEntity<Map> invalid = post(
                "/api/reset-password",
                Map.of("email", ADMIN_EMAIL, "token", "token-invalido", "password", "password12"),
                jsonHeaders(),
                Map.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, invalid.getStatusCode());
        assertTrue(isJson(invalid));
        assertEquals("INVALID_TOKEN", invalid.getBody().get("title"));
    }

    @Test
    void contentTypeIsJsonEvenWhenClientSendsSuspiciousAccept() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml," + MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<String> response = post(
                "/api/login",
                Map.of("email", "bad", "password", XSS_PAYLOAD),
                headers,
                String.class
        );

        assertEquals(UNPROCESSABLE, response.getStatusCode());
        assertTrue(isJson(response));
        assertFalse(response.getBody().contains("<img"));
        assertFalse(response.getBody().toLowerCase().contains("onerror"));
    }

    private String issueResetTokenFor(String email) {
        var user = users.findByEmail(email.toLowerCase()).orElseThrow();
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        var now = clock.now();
        resetTokens.save(PasswordResetToken.issue(
                user.getId(),
                tokenHasher.hash(raw),
                now.plus(Duration.ofHours(1)),
                now
        ));
        return raw;
    }
}
