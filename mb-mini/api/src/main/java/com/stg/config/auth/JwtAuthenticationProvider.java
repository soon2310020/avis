package com.stg.config.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public interface JwtAuthenticationProvider {

    AbstractAuthenticationToken get(String jwt);

}
