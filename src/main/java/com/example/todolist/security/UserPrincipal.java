package com.example.todolist.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class UserPrincipal implements UserDetails {

    private final String username;
    private final String password;
    private final String tenantId;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(String username, String password, String tenantId, Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.password = password;
        this.tenantId = tenantId;
        this.authorities = Collections.unmodifiableCollection(authorities);
    }

    public static UserPrincipal from(com.example.todolist.model.UserAccount account) {
        return new UserPrincipal(
                account.getUsername(),
                account.getPassword(),
                account.getTenantId(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    public String getTenantId() {
        return tenantId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserPrincipal)) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(username, that.username) && Objects.equals(tenantId, that.tenantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, tenantId);
    }
}
