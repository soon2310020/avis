package saleson.common.config;

import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {


	/**
	 * Jackson Afterburner module to speed up serialization/deserialization.
	 */
	@Bean
	public AfterburnerModule afterburnerModule() {
		return new AfterburnerModule();
	}
}
