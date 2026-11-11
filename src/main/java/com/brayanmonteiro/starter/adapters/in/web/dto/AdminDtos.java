package com.brayanmonteiro.starter.adapters.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public final class AdminDtos {
    private AdminDtos() {}

    public record CreateUserRequest(
            @NotBlank String name,
            @Email @NotBlank String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            Set<Long> roleIds
    ) {}

    public record UpdateUserRequest(
            @NotBlank String name,
            @Email @NotBlank String email,
            @Size(min = 8, max = 72) String password,
            Set<Long> roleIds
    ) {}

    public record CreateRoleRequest(@NotBlank String name, Set<Long> permissionIds) {}
    public record UpdateRoleRequest(@NotBlank String name, @NotNull Set<Long> permissionIds) {}
}
