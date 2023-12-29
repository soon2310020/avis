package com.stg.service3rd.common.logger;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.stg.service3rd.common.logger")
@EnableTransactionManagement
public class Jpa3rdLogConfiguration {
}
