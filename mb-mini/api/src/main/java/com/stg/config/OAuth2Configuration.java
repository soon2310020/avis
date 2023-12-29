package com.stg.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableAuthorizationServer
public class OAuth2Configuration extends AuthorizationServerConfigurerAdapter {

    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    private final UserDetailsService userService;

    private final KeyPairProvider keyPairProvider;

    @Value("${web.cors.enabled:false}")
    private boolean corsEnabled;

    @Value("${web.origin.admin}")
    private String allowOriginAdmin;
    @Value("${web.origin.app}")
    private String allowOriginApp;
    @Value("${external.host.mb-host}")
    private String mbHost;

    private static final List<String> ALLOWED_METHODS = Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "OPTIONS", "HEAD", "DELETE"
    );

    @Value("${jwt.clientId:benhost}")
    private String clientId;

    @Value("${jwt.client-secret:secret}")
    private String clientSecret;

    @Value("${jwt.accessTokenValiditySeconds:3600}") // 1 hour
    private int accessTokenValiditySeconds;

    @Value("${jwt.refreshTokenValiditySeconds:43200}") // 12 hours
    private int refreshTokenValiditySeconds;

    private static final String[] GRANT_TYPES = new String[]{"refresh_token", "password", "authorization_code"};

    private static final String SCOPES = "Application";

    public OAuth2Configuration(AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, UserDetailsService userService, KeyPairProvider keyPairProvider) {
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.keyPairProvider = keyPairProvider;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient(clientId)
                .secret(passwordEncoder.encode(clientSecret))
                .accessTokenValiditySeconds(accessTokenValiditySeconds)
                .refreshTokenValiditySeconds(refreshTokenValiditySeconds)
                .authorizedGrantTypes(GRANT_TYPES)
                .autoApprove(true)
                .scopes(SCOPES);
    }

    @Override
    public void configure(final AuthorizationServerEndpointsConfigurer endpoints) {
        CustomTokenEnhancerChain tokenEnhancerChain = new CustomTokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer(), accessTokenConverter()));
        endpoints
                .tokenStore(tokenStore())
                .tokenEnhancer(tokenEnhancerChain)
                .accessTokenConverter(accessTokenConverter())
                .userDetailsService(userService)
                .authenticationManager(authenticationManager)
                .exceptionTranslator(webResponseExceptionTranslator());
    }

    @Bean
    public WebResponseExceptionTranslator webResponseExceptionTranslator() {
        return new CustomWebResponseExceptionTranslator();
    }

    @Bean
    public TokenStore tokenStore() {
        return new AuditingJwtTokenStore(accessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();

        converter.setKeyPair(keyPairProvider.getKeyPair(
                keyPairProvider.getKeyPairs().keySet().stream()
                        .findFirst().orElseThrow(() -> new IllegalArgumentException("No default keypair found!")))
        );

        return converter;
    }

    @Bean
    public TokenEnhancer tokenEnhancer() {
        return new CustomTokenEnhancer();
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> simpleCorsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);

        config.setAllowedOrigins(List.of(allowOriginAdmin, allowOriginApp, mbHost));
        //config.addAllowedOriginPattern("*"); // allow all

        config.setAllowedMethods(ALLOWED_METHODS);
        config.setAllowedHeaders(Collections.singletonList("*"));
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

}