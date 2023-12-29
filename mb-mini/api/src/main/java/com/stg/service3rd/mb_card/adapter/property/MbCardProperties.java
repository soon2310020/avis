package com.stg.service3rd.mb_card.adapter.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@EnableConfigurationProperties(MbCardProperties.class)
@ConfigurationProperties(prefix = "api3rd.mb-card")
public class MbCardProperties {
    private String host;
    private String merchant;
    private String merchantSecret;
}
