package com.stg.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@EnableConfigurationProperties(AsyncProperties.class)
@ConfigurationProperties(prefix = "spring.executor")
public class AsyncProperties {
    private PoolSize schedule;
    private PoolSize normal;
    private PoolSize takesTimeHighCpu;
    private PoolSize takesTimeLowCpu;

    @Getter
    @Setter
    public static class PoolSize {
        private int corePoolSize;
        //private int maxPoolSize; // bỏ qua max pool vì max queue size = MAX INTEGER
    }
}
