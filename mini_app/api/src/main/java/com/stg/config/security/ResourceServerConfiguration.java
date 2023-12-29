package com.stg.config.security;

import com.stg.common.Endpoints;
import com.stg.config.security.auth.JwtTokenAuthenticationFilter;
import com.stg.config.security.basic.CustomBasicAuthenticationFilter;
import com.stg.errors.CustomAccessDeniedHandler;
import com.stg.errors.CustomAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerConfiguration extends WebSecurityConfigurerAdapter {
    private static final List<String> ALLOWED_METHODS = Arrays.asList("GET", "POST", "PUT", "PATCH", "OPTIONS", "HEAD",
            "DELETE");

    @Value("${web.origin.api}")
    private String allowOriginApi;
    @Value("${web.origin.webview}")
    private String allowOriginWebView;

    @Value("${security.web.cors-mapping}")
    private String corsMapping;
    
    @Value("${security.sale-tool-mbal.user}")
    private String saleToolMbalUser;

    @Value("${security.sale-tool-mbal.password}")
    private String saleToolMbalPassword;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    private final JwtTokenAuthenticationFilter jwtTokenAuthenticationFilter;

    private CustomBasicAuthenticationFilter basicAuthenticationFilter;

    @Autowired
    private Environment environment;

    public ResourceServerConfiguration(CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
            JwtTokenAuthenticationFilter jwtTokenAuthenticationFilter,
            CustomAccessDeniedHandler customAccessDeniedHandler) {
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.jwtTokenAuthenticationFilter = jwtTokenAuthenticationFilter;
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        PasswordEncoder encoder = passwordEncoder();
        auth.inMemoryAuthentication()
                .withUser(saleToolMbalUser).password(encoder.encode(saleToolMbalPassword)).roles("INTEGRATE")
        ;
    }

    @Autowired
    public void setBasicAuthenticationFilter(AuthenticationManager manager) {
        this.basicAuthenticationFilter = new CustomBasicAuthenticationFilter(manager);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .headers()
                .frameOptions().sameOrigin()
                .and()
                .csrf().disable()
                .addFilterBefore(jwtTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(basicAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/v1/signin/**").permitAll()
                .antMatchers("/v1/forgotPassword").permitAll()
                .antMatchers("/v1/signin**").permitAll()
                .antMatchers("/v1/users/token").permitAll()
                .antMatchers("/actuator/health").permitAll()
                .antMatchers(Endpoints.URL_CRM_VERIFY).permitAll()
                .antMatchers(Endpoints.URL_REFRESH_TOKEN).permitAll()
                .antMatchers(Endpoints.QUOTATION.URL + "/{\\d+}/crm-download").permitAll()
                .antMatchers("/combo/{\\d+}/crm-download").permitAll()
                .antMatchers(Endpoints.LEAD.SYNC).hasRole("INTEGRATE") // handle phân quyền api ở phase 2
                .antMatchers("/admin/v1/**").hasRole("ADMIN")
                .antMatchers("/v1/**").hasRole("API")
                .anyRequest().authenticated()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    @Override
    public void configure(WebSecurity web) {
        List<String> swaggerEnabledProfiles = Arrays.asList("dev", "stg");
        if (Arrays.stream(environment.getActiveProfiles()).anyMatch(swaggerEnabledProfiles::contains)) {
            web.ignoring().antMatchers("/v2/api-docs/**");
            web.ignoring().antMatchers("/swagger.json");
            web.ignoring().antMatchers("/swagger-ui/**");
            web.ignoring().antMatchers("/swagger-resources/**");
            web.ignoring().antMatchers("/webjars/**");
        }
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> simpleCorsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);

        if ("*".equals(corsMapping)) { // handle on infrastructure
            config.addAllowedOriginPattern("*");
        } else {
            // move allowOriginApi, allowOriginWebView into corsMapping...!
            String oldCORS = String.join(",", List.of(allowOriginApi, allowOriginWebView));
            corsMapping = StringUtils.hasText(corsMapping) ? corsMapping + "," + oldCORS : oldCORS;

            if (StringUtils.hasText(corsMapping)) {
                String[] corsArray = corsMapping.split(",");
                for (String cors : corsArray) {
                    config.addAllowedOrigin(cors.trim());
                }
            } else {
                config.addAllowedOriginPattern("*");
            }
        }

        config.setAllowedMethods(ALLOWED_METHODS);
        config.setAllowedHeaders(Collections.singletonList("*"));
        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

}