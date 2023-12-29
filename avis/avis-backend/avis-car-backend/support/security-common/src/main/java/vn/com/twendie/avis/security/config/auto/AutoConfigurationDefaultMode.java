package vn.com.twendie.avis.security.config.auto;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import vn.com.twendie.avis.security.core.filter.TokenProvider;
import vn.com.twendie.avis.security.core.service.CustomTokenService;
import vn.com.twendie.avis.security.jdbc.provider.JdbcTokenProviderImpl;

@Configuration
public class AutoConfigurationDefaultMode {

    private final DataSource dataSource;

    private final UserDetailsService avisUserDetail;

    public AutoConfigurationDefaultMode(DataSource dataSource,
                                        @Qualifier("avisUserDetailService") UserDetailsService avisUserDetail) {
        this.dataSource = dataSource;
        this.avisUserDetail = avisUserDetail;
    }

    @Bean
    @ConditionalOnMissingBean(TokenStore.class)
    public TokenStore tokenStore() {
        return new JdbcTokenStore(dataSource);
    }

    @Bean
    @Lazy
    @ConditionalOnMissingBean(TokenProvider.class)
    public TokenProvider defaultTokenProvider(@Autowired TokenStore tokenStore,
                                              @Autowired CustomTokenService customTokenService) {
        return new JdbcTokenProviderImpl(tokenStore, avisUserDetail, customTokenService);
    }

    @Bean
    @ConditionalOnMissingBean(DefaultTokenServices.class)
    public DefaultTokenServices tokenServices() {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore());
        defaultTokenServices.setSupportRefreshToken(true);
        return defaultTokenServices;
    }
}
