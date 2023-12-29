package com.stg.config;

import com.stg.config.auth.JwtAuthenticationProvider;
import com.stg.config.auth.PublicKeyProvider;
import com.stg.config.auth.impl.LegacyJwtAuthenticationProvider;
import com.stg.repository.BlackListTokenRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtAuthenticationConfiguration {

    @Bean
    public JwtAuthenticationProvider jwtAuthenticationProvider(PublicKeyProvider publicKeyProvider, BlackListTokenRepository blackListTokenRepository) {
        return new LegacyJwtAuthenticationProvider(publicKeyProvider, blackListTokenRepository);
    }

}
