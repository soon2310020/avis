package com.stg.config;

import com.stg.service.dto.baas.OauthToken;

public interface AccessTokenFactory {

    OauthToken createAccessToken(String token, String mbId);

    OauthToken genAccessTokenFromRefreshToken(String refreshToken);

}
