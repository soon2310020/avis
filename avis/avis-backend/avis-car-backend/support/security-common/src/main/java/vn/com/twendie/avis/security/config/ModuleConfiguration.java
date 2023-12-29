package vn.com.twendie.avis.security.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import vn.com.twendie.avis.api.rest.config.EnableRestCommonApi;

@ComponentScan({ "vn.com.twendie.avis.security.core" })
@EnableJpaRepositories("vn.com.twendie.avis.security.jdbc.repository")
@Configuration
@EnableRestCommonApi
public class ModuleConfiguration {
}
