package com.stg.service3rd.mic_sandbox.adapter.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@EnableConfigurationProperties(MicSandboxProperties.class)
@ConfigurationProperties(prefix = "external.mic-key")
public class MicSandboxProperties {
    private String username;
    private String password;
    private String clientId;
    private String clientSecret;
}