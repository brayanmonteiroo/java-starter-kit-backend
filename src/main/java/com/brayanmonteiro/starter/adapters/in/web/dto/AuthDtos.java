package com.brayanmonteiro.starter.adapters.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}
    public record LogoutRequest(String refreshToken) {}
    public record ForgotPasswordRequest(@Email @NotBlank String email) {}
    public record ResetPasswordRequest(
            @Email @NotBlank String email,
            @NotBlank String token,
            @NotBlank @Size(min = 8, max = 72) String password
    ) {}
    public record TokenResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {}

    /** Atualização do próprio perfil (não altera roles — isso é admin). */
    public record UpdateProfileRequest(
            @NotBlank String name,
            @Email @NotBlank String email,
            @Size(min = 8, max = 72) String password
    ) {}
}
