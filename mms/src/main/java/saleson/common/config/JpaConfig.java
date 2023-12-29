package saleson.common.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = { "com.emoldino", "saleson" })
@EntityScan(basePackages = { "com.emoldino", "saleson" })
@Configuration
public class JpaConfig {

}