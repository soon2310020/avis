package com.stg.service3rd.mic.adapter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@EnableConfigurationProperties(MicProperties.class)
@ConfigurationProperties(prefix = "api3rd.mic")
public class MicProperties {
    private String host;
    private String merchantSecret;
    private String dviCode;
    private String nsd;
    private String pas;
//    private String bankAccount;
//    private String accountName;
//    private String transferFee;
}
