package com.stg.config;

import com.stg.config.auth.impl.CustomUserDetails;
import com.stg.service.impl.UserDetailsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsServiceImpl userDetailsServiceImpl;

    public CustomAuthenticationProvider(UserDetailsServiceImpl userDetailsServiceImpl) {
        this.userDetailsServiceImpl = userDetailsServiceImpl;
    }

    @Override
    public boolean supports(Class aClass) {
        return true;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        CustomUserDetails customUserDetails = null;
        try {
            String enteredPassword = (String) authentication.getCredentials();
            customUserDetails = (CustomUserDetails) userDetailsServiceImpl.loadUser(authentication.getPrincipal().toString(), enteredPassword);
            return new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        } catch (Exception e) {
            log.error("[MINI]--Authentication failed for {}", customUserDetails);
            throw e;
        }
    }
}