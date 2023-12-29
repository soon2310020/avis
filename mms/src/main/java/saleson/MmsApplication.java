package saleson;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.filter.HiddenHttpMethodFilter;

import saleson.common.hibernate.auditor.SecurityAuditorAware;

@SpringBootApplication(scanBasePackages = { "com.emoldino", "saleson" })
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableScheduling
@EnableAsync
public class MmsApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(MmsApplication.class);
		application.addListeners(new ApplicationPidFileWriter());
		application.run(args);
	}

	@Bean
	HiddenHttpMethodFilter hiddenHttpMethodFilter() {
		return new HiddenHttpMethodFilter();
	}

	@Bean
	public AuditorAware<Long> auditorAware() {
		return new SecurityAuditorAware();
	}
}
