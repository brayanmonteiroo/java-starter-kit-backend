package com.brayanmonteiro.starter.domain.access;

import java.util.List;

public final class PermissionNames {
    public static final String DASHBOARD_SIDEBAR = "dashboard.sidebar";
    public static final String DASHBOARD_VIEW = "dashboard.view";
    public static final String DASHBOARD_CARDS = "dashboard.cards";
    public static final String USERS_SIDEBAR = "users.sidebar";
    public static final String USERS_VIEW = "users.view";
    public static final String USERS_SHOW = "users.show";
    public static final String USERS_CREATE = "users.create";
    public static final String USERS_UPDATE = "users.update";
    public static final String USERS_DELETE = "users.delete";
    public static final String ROLES_SIDEBAR = "roles.sidebar";
    public static final String ROLES_VIEW = "roles.view";
    public static final String ROLES_SHOW = "roles.show";
    public static final String ROLES_CREATE = "roles.create";
    public static final String ROLES_UPDATE = "roles.update";
    public static final String ROLES_DELETE = "roles.delete";
    public static final String AUDIT_SIDEBAR = "audit.sidebar";
    public static final String AUDIT_VIEW = "audit.view";

    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_USER = "user";

    private PermissionNames() {}

    public static List<String> all() {
        return List.of(
                DASHBOARD_SIDEBAR, DASHBOARD_VIEW, DASHBOARD_CARDS,
                USERS_SIDEBAR, USERS_VIEW, USERS_SHOW, USERS_CREATE, USERS_UPDATE, USERS_DELETE,
                ROLES_SIDEBAR, ROLES_VIEW, ROLES_SHOW, ROLES_CREATE, ROLES_UPDATE, ROLES_DELETE,
                AUDIT_SIDEBAR, AUDIT_VIEW
        );
    }

    public static List<String> userDefaults() {
        return List.of(DASHBOARD_SIDEBAR, DASHBOARD_VIEW, DASHBOARD_CARDS);
    }
}
