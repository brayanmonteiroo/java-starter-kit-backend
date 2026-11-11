package com.brayanmonteiro.starter.adapters.in.web.admin;

import com.brayanmonteiro.starter.adapters.in.web.dto.AdminDtos;
import com.brayanmonteiro.starter.adapters.in.web.shared.SecuritySupport;
import com.brayanmonteiro.starter.application.admin.AuditAdminUseCases;
import com.brayanmonteiro.starter.application.admin.DashboardUseCase;
import com.brayanmonteiro.starter.application.admin.RoleAdminUseCases;
import com.brayanmonteiro.starter.application.admin.UserAdminUseCases;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminControllers {
    private final DashboardUseCase dashboardUseCase;
    private final UserAdminUseCases userAdminUseCases;
    private final RoleAdminUseCases roleAdminUseCases;
    private final AuditAdminUseCases auditAdminUseCases;

    public AdminControllers(DashboardUseCase dashboardUseCase, UserAdminUseCases userAdminUseCases,
                            RoleAdminUseCases roleAdminUseCases, AuditAdminUseCases auditAdminUseCases) {
        this.dashboardUseCase = dashboardUseCase;
        this.userAdminUseCases = userAdminUseCases;
        this.roleAdminUseCases = roleAdminUseCases;
        this.auditAdminUseCases = auditAdminUseCases;
    }

    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public Object dashboard(@RequestParam(defaultValue = "7") int days) {
        return dashboardUseCase.execute(days);
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('users.view')")
    public Object users(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return userAdminUseCases.list(page, size);
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAuthority('users.show')")
    public Object user(@PathVariable Long id) {
        return userAdminUseCases.get(id);
    }

    @PostMapping("/users")
    @PreAuthorize("hasAuthority('users.create')")
    @ResponseStatus(HttpStatus.CREATED)
    public Object createUser(@Valid @RequestBody AdminDtos.CreateUserRequest request, HttpServletRequest http) {
        return userAdminUseCases.create(request.name(), request.email(), request.password(), request.roleIds(),
                SecuritySupport.auditContext(http));
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('users.update')")
    public Object updateUser(@PathVariable Long id, @Valid @RequestBody AdminDtos.UpdateUserRequest request, HttpServletRequest http) {
        return userAdminUseCases.update(id, request.name(), request.email(), request.password(), request.roleIds(),
                SecuritySupport.auditContext(http));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('users.delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id, HttpServletRequest http) {
        userAdminUseCases.delete(id, SecuritySupport.auditContext(http));
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('roles.view')")
    public Object roles() {
        return roleAdminUseCases.list();
    }

    @GetMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('roles.show')")
    public Object role(@PathVariable Long id) {
        return roleAdminUseCases.get(id);
    }

    @GetMapping("/permissions/catalog")
    @PreAuthorize("hasAuthority('roles.view')")
    public Object permissions() {
        return roleAdminUseCases.catalog();
    }

    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('roles.create')")
    @ResponseStatus(HttpStatus.CREATED)
    public Object createRole(@Valid @RequestBody AdminDtos.CreateRoleRequest request, HttpServletRequest http) {
        return roleAdminUseCases.create(request.name(), request.permissionIds(), SecuritySupport.auditContext(http));
    }

    @PutMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('roles.update')")
    public Object updateRole(@PathVariable Long id, @Valid @RequestBody AdminDtos.UpdateRoleRequest request, HttpServletRequest http) {
        return roleAdminUseCases.update(id, request.name(), request.permissionIds(), SecuritySupport.auditContext(http));
    }

    @DeleteMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('roles.delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRole(@PathVariable Long id, HttpServletRequest http) {
        roleAdminUseCases.delete(id, SecuritySupport.auditContext(http));
    }

    @GetMapping("/audits")
    @PreAuthorize("hasAuthority('audit.view')")
    public Object audits(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return auditAdminUseCases.list(page, size);
    }

    @GetMapping("/audits/{id}")
    @PreAuthorize("hasAuthority('audit.view')")
    public Object audit(@PathVariable Long id) {
        return auditAdminUseCases.get(id);
    }
}
