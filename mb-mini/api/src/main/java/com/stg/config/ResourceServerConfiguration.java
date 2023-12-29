package com.stg.config;

import com.stg.errors.CustomAccessDeniedHandler;
import com.stg.errors.CustomAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Objects;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    private final JwtTokenAuthenticationFilter jwtTokenAuthenticationFilter;

    public ResourceServerConfiguration(CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
                                       JwtTokenAuthenticationFilter jwtTokenAuthenticationFilter) {
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.jwtTokenAuthenticationFilter = jwtTokenAuthenticationFilter;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .headers()
                .frameOptions().sameOrigin()
                .and()
                .csrf().disable()
                .addFilterBefore(jwtTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/external/mb/session/verify").permitAll()
                .antMatchers("/external/mb/callback/transaction").permitAll()
                .antMatchers("/api/forgotPassword").permitAll()
                .antMatchers("/api/users/token").permitAll()
                .antMatchers("/actuator/health").permitAll()
                .antMatchers("/api/users/changePassword/confirmation").permitAll()
                .antMatchers("/api/users/changePassword/reset").permitAll()
                .antMatchers("/api/healthQuestion/twelveQuestion").permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .accessDeniedHandler(new CustomAccessDeniedHandler());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    public void configure(WebSecurity web) {
        if (Objects.equals(activeProfile, "stg") || Objects.equals(activeProfile, "dev")) {
            web.ignoring().antMatchers("/v3/api-docs/**", "/swagger-ui/**");
        }
    }
}