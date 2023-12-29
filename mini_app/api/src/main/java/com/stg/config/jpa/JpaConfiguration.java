package com.stg.config.jpa;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.stg.repository")
@EnableJpaAuditing(auditorAwareRef = "springAuditorAware")
@EnableTransactionManagement
public class JpaConfiguration {

}
