package com.brayanmonteiro.starter.adapters.in.web.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AuthenticatedUser implements UserDetails {
    private final Long id;
    private final String email;
    private final String name;
    private final Set<String> roles;
    private final Set<String> permissions;

    public AuthenticatedUser(Long id, String email, String name, Set<String> roles, Set<String> permissions) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.roles = roles;
        this.permissions = permissions;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public Set<String> getRoles() { return roles; }
    public Set<String> getPermissions() { return permissions; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Stream.concat(
                roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase())),
                permissions.stream().map(SimpleGrantedAuthority::new)
        ).collect(Collectors.toSet());
    }

    @Override public String getPassword() { return ""; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
