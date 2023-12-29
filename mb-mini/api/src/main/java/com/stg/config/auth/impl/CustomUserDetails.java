package com.stg.config.auth.impl;

import com.stg.errors.ApplicationException;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Data
@Accessors(chain = true)
public class CustomUserDetails implements UserDetails {

    private String userId;
    private String email;
    private String role;
    private List<String> features;

    public CustomUserDetails(String userId, String email, String role, List<String> features) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.features = features;
    }

    public CustomUserDetails() {

    }

    @Override
    public String getPassword() {
        throw new ApplicationException("Calls to UserDetails.getPassword() are forbidden");
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return true;
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
    public Collection<GrantedAuthority> getAuthorities() {
        return Collections.emptySet();
    }
}
