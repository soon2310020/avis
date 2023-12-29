package vn.com.twendie.avis.mobile.api.config;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import vn.com.twendie.avis.mobile.api.filter.DriverRoleRequestFilter;
import vn.com.twendie.avis.security.core.filter.TokenRequestFilter;

@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final TokenRequestFilter tokenRequestFilter;
    private final DriverRoleRequestFilter driverRoleRequestFilter;

    public WebSecurityConfig(@Lazy TokenRequestFilter tokenRequestFilter,
                             DriverRoleRequestFilter driverRoleRequestFilter) {
        this.tokenRequestFilter = tokenRequestFilter;
        this.driverRoleRequestFilter = driverRoleRequestFilter;
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.cors().and()
                .csrf().disable()
                .logout().disable()
                .formLogin().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .anonymous().and()
                .addFilterBefore(tokenRequestFilter, BasicAuthenticationFilter.class)
                .addFilterAfter(driverRoleRequestFilter, BasicAuthenticationFilter.class)
                .antMatcher("/**")
                .authorizeRequests()
                .anyRequest()
                .authenticated();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources/**",
                "/configuration/security", "/swagger-ui.html", "/webjars/**","/notifications/write-logs-client");
    }

}
