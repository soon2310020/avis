package com.stg.config.jackson;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {
    @Bean("jackson")
    public Jackson jackson() {
        return Jackson.newInstance();
    }

}
