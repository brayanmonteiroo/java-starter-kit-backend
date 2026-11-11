package com.brayanmonteiro.starter.adapters.in.web.shared;

import com.brayanmonteiro.starter.adapters.in.web.security.AuthenticatedUser;
import com.brayanmonteiro.starter.application.shared.AuditContext;
import com.brayanmonteiro.starter.domain.shared.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecuritySupport {
    private SecuritySupport() {}

    public static AuthenticatedUser requireUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new UnauthorizedException("Não autenticado");
        }
        return user;
    }

    public static AuditContext auditContext(HttpServletRequest request) {
        AuthenticatedUser user = requireUser();
        return new AuditContext(user.getId(), user.getEmail(), request.getRemoteAddr(), request.getHeader("User-Agent"));
    }
}
