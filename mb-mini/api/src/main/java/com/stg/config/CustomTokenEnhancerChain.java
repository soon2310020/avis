package com.stg.config;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
@Service
@NoArgsConstructor
@Slf4j
public class CustomTokenEnhancerChain implements TokenEnhancer {

    private List<TokenEnhancer> delegates = Collections.emptyList();

    /**
     * @param delegates the delegates to set
     */
    public void setTokenEnhancers(List<TokenEnhancer> delegates) {
        this.delegates = delegates;
    }

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {

        OAuth2AccessToken result = accessToken;
        for (TokenEnhancer enhancer : delegates) {
            result = enhancer.enhance(result, authentication);
        }

        return result;
    }
}