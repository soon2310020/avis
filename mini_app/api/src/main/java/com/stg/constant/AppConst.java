package com.stg.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AppConst {
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class TokenType {
        public static final String AUTHORIZATION = "Authorization";
        public static final String X_AUTH_TOKEN = "X-Auth-Token";
        public static final String CLIENT_ID = "Client-Id";
    }

}
