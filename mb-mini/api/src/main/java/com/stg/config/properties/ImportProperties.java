package com.stg.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@EnableConfigurationProperties(ImportProperties.class)
@ConfigurationProperties(prefix = "import")
public class ImportProperties {
    private boolean rmRefresh;
}
