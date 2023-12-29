package com.stg.config;


import com.stg.config.auth.impl.CustomUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.HashMap;
import java.util.Map;


@AllArgsConstructor
public class CustomTokenEnhancer implements TokenEnhancer {

    public static final String USER_ID_KEY = "userId";
    public static final String ROLE = "role";
    private static final String FEATURES = "features";

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        Map<String, Object> additionalInfo = new HashMap<>();

        additionalInfo.put(ROLE, ((CustomUserDetails) authentication.getPrincipal()).getRole());
        additionalInfo.put(USER_ID_KEY, ((CustomUserDetails) authentication.getPrincipal()).getUserId());
        additionalInfo.put(FEATURES, ((CustomUserDetails) authentication.getPrincipal()).getFeatures());

        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);

        return accessToken;
    }
}