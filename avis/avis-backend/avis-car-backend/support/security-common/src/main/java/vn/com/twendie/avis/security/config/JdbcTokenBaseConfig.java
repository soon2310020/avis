package vn.com.twendie.avis.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import vn.com.twendie.avis.security.core.service.CustomTokenService;

import javax.sql.DataSource;
import java.time.Duration;

@Configuration
@ComponentScan({"vn.com.twendie.avis.security.jdbc"})
@Primary
public class JdbcTokenBaseConfig {

	@Value("${authen.jwt.expiredTime}")
	private Duration expiredTime;

	private final DataSource dataSource;

	public JdbcTokenBaseConfig(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Bean
	public TokenStore tokenStore() {
		return new JdbcTokenStore(dataSource);
	}

	@Bean
	public CustomTokenService tokenServices() {
		CustomTokenService customTokenService = new CustomTokenService();
		customTokenService.setTokenStore(tokenStore());
		customTokenService.setAccessTokenValiditySeconds((int) expiredTime.getSeconds());
		customTokenService.setSupportRefreshToken(true);
		return customTokenService;
	}
}
