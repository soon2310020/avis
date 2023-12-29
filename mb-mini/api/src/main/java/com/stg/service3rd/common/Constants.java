package com.stg.service3rd.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {
    public static final String CURRENT_SERVER_NAME = "server.miniapp";

    /*public enum HostParty {
        MBAL, MIC, MB, BAAS, CRM, TOOL_CRM
    }*/

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class HeaderKey {
        public static final String AUTHORIZATION = "Authorization";
        public static final String X_AUTH_TOKEN = "X-Auth-Token";
        public static final String CLIENT_ID = "Client-Id";
        public static final String BEARER = "Bearer ";
        public static final String GRANT_TYPE = "grant_type";
        public static final String CLIENT_CREDENTIALS = "client_credentials";

        public static final String CLIENT_REQUEST_ID = "CX-Request-Id"; // requestId for this server
        public static final String X_REQUEST_ID = "X-Request-ID"; // requestId for MBAL,...
        public static final String CLIENT_MESSAGE_ID = "clientMessageId"; // requestId for BAAS,...
    }
}
