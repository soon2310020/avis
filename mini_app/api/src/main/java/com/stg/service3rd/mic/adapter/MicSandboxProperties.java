package com.stg.service3rd.mic.adapter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@EnableConfigurationProperties(MicSandboxProperties.class)
@ConfigurationProperties(prefix = "api3rd.mic-sandbox")
public class MicSandboxProperties {
    private String host;
    private String username;
    private String password;
    private String clientId;
    private String clientSecret;
}
