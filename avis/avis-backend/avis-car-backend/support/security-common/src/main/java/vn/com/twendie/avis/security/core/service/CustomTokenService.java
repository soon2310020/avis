package vn.com.twendie.avis.security.core.service;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

public class CustomTokenService extends DefaultTokenServices {

    private TokenStore tokenStore;

    @Override
    public void setTokenStore(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
        super.setTokenStore(tokenStore);
    }

    @Transactional
    @Override
    public OAuth2AccessToken createAccessToken(OAuth2Authentication authentication) throws AuthenticationException {

        OAuth2RefreshToken refreshToken = new DefaultOAuth2RefreshToken(UUID.randomUUID().toString());
        OAuth2AccessToken accessToken = createAccessToken(authentication, refreshToken);

        tokenStore.storeAccessToken(accessToken, authentication);
        tokenStore.storeRefreshToken(accessToken.getRefreshToken(), authentication);
        return accessToken;

    }

    private OAuth2AccessToken createAccessToken(OAuth2Authentication authentication, OAuth2RefreshToken refreshToken) {
        DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(UUID.randomUUID().toString());
        int validitySeconds = getAccessTokenValiditySeconds(authentication.getOAuth2Request());
        if (validitySeconds > 0) {
            token.setExpiration(new Date(System.currentTimeMillis() + (validitySeconds * 1000L)));
        }
        token.setRefreshToken(refreshToken);
        token.setScope(authentication.getOAuth2Request().getScope());

        return token;
    }

    @Transactional(noRollbackFor={InvalidGrantException.class})
    public OAuth2AccessToken refreshAccessToken(String refreshTokenValue, String accessTokenValue) throws AuthenticationException {

        OAuth2RefreshToken refreshToken = tokenStore.readRefreshToken(refreshTokenValue);
        if (refreshToken == null) {
            throw new InvalidGrantException("Invalid refresh token: " + refreshTokenValue);
        }

        OAuth2Authentication authentication = tokenStore.readAuthenticationForRefreshToken(refreshToken);
        DefaultOAuth2AccessToken accessToken = (DefaultOAuth2AccessToken) tokenStore.readAccessToken(accessTokenValue);

        accessToken.setExpiration(new Date(System.currentTimeMillis() + (Integer.MAX_VALUE * 1000L)));
        tokenStore.storeAccessToken(accessToken, authentication);

        return accessToken;
    }

    @Transactional
    public void extendExpiredTime(OAuth2Authentication authentication, DefaultOAuth2AccessToken accessToken) throws AuthenticationException {

        int validitySeconds = getAccessTokenValiditySeconds(authentication.getOAuth2Request());

        if (validitySeconds - accessToken.getExpiresIn() > 300) {
            accessToken.setExpiration(new Date(System.currentTimeMillis() + (validitySeconds * 1000L)));
            tokenStore.storeAccessToken(accessToken, authentication);
        }

    }
}
