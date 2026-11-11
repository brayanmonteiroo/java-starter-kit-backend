package com.brayanmonteiro.starter.application.admin;

import java.util.List;

public record PermissionCatalogView(List<PermissionItem> permissions) {
    public record PermissionItem(Long id, String name) {}
}
