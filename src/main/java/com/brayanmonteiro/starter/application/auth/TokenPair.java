package com.brayanmonteiro.starter.application.auth;

public record TokenPair(String accessToken, String refreshToken, long expiresInSeconds) {}
