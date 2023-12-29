package vn.com.twendie.avis.security.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import vn.com.twendie.avis.security.config.auto.AutoConfigurationDefaultMode;

@Import({ ModuleConfiguration.class, JdbcTokenBaseConfig.class, AutoConfigurationDefaultMode.class,
		JwtTokenBaseConfig.class })
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableSecurityModule {

}
