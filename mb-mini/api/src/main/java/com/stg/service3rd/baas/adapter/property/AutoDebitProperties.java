package com.stg.service3rd.baas.adapter.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@EnableConfigurationProperties(AutoDebitProperties.class)
@ConfigurationProperties(prefix = "api3rd.baas.auto-debit")
public class AutoDebitProperties {
    private String host;
    private String clientId;
    private String clientSecret;
}
