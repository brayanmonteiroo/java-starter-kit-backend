package com.brayanmonteiro.starter.domain.shared;

public class NotFoundException extends DomainException {
    public NotFoundException(String message) {
        super("NOT_FOUND", message);
    }
}
