package com.seguranca.plataforma.auth;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class JwtAuthenticatedUser implements UserDetails {
    private final String username;
    private final Collection<? extends GrantedAuthority> authorities;

    public JwtAuthenticatedUser(String username, Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return username;
    }
}


