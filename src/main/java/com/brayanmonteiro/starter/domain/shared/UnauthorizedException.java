package com.brayanmonteiro.starter.domain.shared;

public class UnauthorizedException extends DomainException {
    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }
}
