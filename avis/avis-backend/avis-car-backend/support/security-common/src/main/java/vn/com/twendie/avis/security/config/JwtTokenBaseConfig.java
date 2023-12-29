package vn.com.twendie.avis.security.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@ComponentScan("vn.com.twendie.avis.security.jwt")
@Order(1000)
public class JwtTokenBaseConfig extends WebSecurityConfigurerAdapter {

}
