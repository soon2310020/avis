package com.stg.config;

import com.stg.config.auth.JwtAuthenticationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class JwtTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";

    private final JwtAuthenticationProvider jwtAuthenticationProvider;

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;


    public JwtTokenAuthenticationFilter(JwtAuthenticationProvider jwtAuthenticationProvider) {
        this.jwtAuthenticationProvider = jwtAuthenticationProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {
        try {
            String jwtToken = extractToken(request);
            if (jwtToken != null) {
                AbstractAuthenticationToken authentication = jwtAuthenticationProvider.get(jwtToken);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("[MINI]--Spring Security Filter Chain Exception: {}", e.getMessage());
            resolver.resolveException(request, response, null, e);
        }
    }

    private String extractToken(HttpServletRequest request) {
        String value = request.getHeader(AUTHORIZATION);
        if (value != null && value.toLowerCase().startsWith(OAuth2AccessToken.BEARER_TYPE.toLowerCase())) {
            return value.substring(OAuth2AccessToken.BEARER_TYPE.length()).trim();
        }

        return null;
    }

    @Override
    public void destroy() {
        log.info("destroy");
    }
}