package com.brayanmonteiro.starter.domain.shared;

public class ConflictException extends DomainException {
    public ConflictException(String message) {
        super("CONFLICT", message);
    }
}
