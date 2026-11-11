package com.brayanmonteiro.starter.application.auth;

import java.util.Set;

public record AuthUserView(Long id, String name, String email, Set<String> roles, Set<String> permissions) {}
