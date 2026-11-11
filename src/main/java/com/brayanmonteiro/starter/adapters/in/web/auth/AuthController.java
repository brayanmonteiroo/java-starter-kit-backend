package com.brayanmonteiro.starter.adapters.in.web.auth;

import com.brayanmonteiro.starter.adapters.in.web.dto.AuthDtos;
import com.brayanmonteiro.starter.adapters.in.web.shared.SecuritySupport;
import com.brayanmonteiro.starter.application.auth.ForgotPasswordUseCase;
import com.brayanmonteiro.starter.application.auth.GetCurrentUserUseCase;
import com.brayanmonteiro.starter.application.auth.LoginUseCase;
import com.brayanmonteiro.starter.application.auth.LogoutUseCase;
import com.brayanmonteiro.starter.application.auth.RefreshTokenUseCase;
import com.brayanmonteiro.starter.application.auth.ResetPasswordUseCase;
import com.brayanmonteiro.starter.application.auth.UpdateMyProfileUseCase;
import com.brayanmonteiro.starter.application.auth.TokenPair;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final UpdateMyProfileUseCase updateMyProfileUseCase;

    public AuthController(LoginUseCase loginUseCase, RefreshTokenUseCase refreshTokenUseCase, LogoutUseCase logoutUseCase,
                          GetCurrentUserUseCase getCurrentUserUseCase, ForgotPasswordUseCase forgotPasswordUseCase,
                          ResetPasswordUseCase resetPasswordUseCase, UpdateMyProfileUseCase updateMyProfileUseCase) {
        this.loginUseCase = loginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.forgotPasswordUseCase = forgotPasswordUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.updateMyProfileUseCase = updateMyProfileUseCase;
    }

    @PostMapping("/login")
    public AuthDtos.TokenResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return toResponse(loginUseCase.execute(request.email(), request.password()));
    }

    @PostMapping("/auth/refresh")
    public AuthDtos.TokenResponse refresh(@Valid @RequestBody AuthDtos.RefreshRequest request) {
        return toResponse(refreshTokenUseCase.execute(request.refreshToken()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody(required = false) AuthDtos.LogoutRequest request) {
        if (request != null) {
            logoutUseCase.execute(request.refreshToken());
        }
    }

    @GetMapping("/user")
    public Object me() {
        return getCurrentUserUseCase.execute(SecuritySupport.requireUser().getId());
    }

    @PutMapping("/user/profile")
    public Object updateProfile(@Valid @RequestBody AuthDtos.UpdateProfileRequest request) {
        return updateMyProfileUseCase.execute(
                SecuritySupport.requireUser().getId(),
                request.name(),
                request.email(),
                request.password()
        );
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void forgot(@Valid @RequestBody AuthDtos.ForgotPasswordRequest request) {
        forgotPasswordUseCase.execute(request.email());
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reset(@Valid @RequestBody AuthDtos.ResetPasswordRequest request) {
        resetPasswordUseCase.execute(request.email(), request.token(), request.password());
    }

    private AuthDtos.TokenResponse toResponse(TokenPair pair) {
        return new AuthDtos.TokenResponse(pair.accessToken(), pair.refreshToken(), "Bearer", pair.expiresInSeconds());
    }
}
