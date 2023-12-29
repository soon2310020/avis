package vn.com.twendie.avis.authen;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import vn.com.twendie.avis.api.rest.config.EnableRestCommonApi;
import vn.com.twendie.avis.data.config.EnableDataModel;
import vn.com.twendie.avis.locale.config.EnableLocaleModule;
import vn.com.twendie.avis.security.config.EnableSecurityModule;

@SpringBootApplication
@EnableDataModel
@EnableSecurityModule
@EnableRestCommonApi
@EnableLocaleModule
@EnableSwagger2
@Slf4j
public class AuthenApplication {
	public static void main(String[] args) {
		SpringApplication.run(AuthenApplication.class, args);
		log.info("Start authentication center service");
	}
}
