package com.brayanmonteiro.starter.domain.identity;

public interface TokenHasherPort {
    String hash(String rawToken);
}
