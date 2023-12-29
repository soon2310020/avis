package vn.com.twendie.avis.data.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan("vn.com.twendie.avis.data")
@EnableJpaRepositories("vn.com.twendie.avis.data.repository")
@ComponentScan("vn.com.twendie.avis.data.service")
public class ModuleConfiguration {

}
