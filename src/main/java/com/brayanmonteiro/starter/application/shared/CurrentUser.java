package com.brayanmonteiro.starter.application.shared;

import java.util.Set;

public record CurrentUser(Long id, String email, String name, Set<String> permissions, Set<String> roles) {}
