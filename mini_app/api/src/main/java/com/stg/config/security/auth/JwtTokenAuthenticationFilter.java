package com.stg.config.security.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.stg.constant.AppConst.TokenType.AUTHORIZATION;
import static com.stg.constant.AppConst.TokenType.CLIENT_ID;
import static com.stg.constant.AppConst.TokenType.X_AUTH_TOKEN;

@Component
@Slf4j
public class JwtTokenAuthenticationFilter extends OncePerRequestFilter {

    private final JwtAuthenticationProvider jwtAuthenticationProvider;

    public JwtTokenAuthenticationFilter(JwtAuthenticationProvider jwtAuthenticationProvider) {
        this.jwtAuthenticationProvider = jwtAuthenticationProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        try {
            String jwtToken = getBearToken(request);
            if (jwtToken != null) {
                AbstractAuthenticationToken authentication = jwtAuthenticationProvider.getAuthentication(jwtToken);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                String xAuthToken = this.getXAuthToken(request);
                if (xAuthToken != null) {
                    AbstractAuthenticationToken xAuthentication = jwtAuthenticationProvider.getAuthInternalServer(this.getClientId(request), xAuthToken);
                    if (xAuthentication != null) {
                        xAuthentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(xAuthentication);
                    }
                }
            }
        } catch (Exception e) {
            log.debug(e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private String getBearToken(HttpServletRequest request) {
        String value = request.getHeader(AUTHORIZATION);
        if (value != null && value.toLowerCase().startsWith(OAuth2AccessToken.BEARER_TYPE.toLowerCase())) {
            return value.substring(OAuth2AccessToken.BEARER_TYPE.length()).trim();
        }

        return null;
    }

    private String getXAuthToken(HttpServletRequest request) {
        return request.getHeader(X_AUTH_TOKEN);
    }

    private String getClientId(HttpServletRequest request) {
        return request.getHeader(CLIENT_ID);
    }

    @Override
    public void destroy() {
        log.info("destroy");
    }
}