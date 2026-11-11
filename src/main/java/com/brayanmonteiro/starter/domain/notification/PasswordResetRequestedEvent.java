package com.brayanmonteiro.starter.domain.notification;

public record PasswordResetRequestedEvent(String email, String name, String resetUrl) {}
