package com.stg.adapter.service;

import com.stg.service.dto.baas.OauthToken;

public interface GetTokenMBAdapterIService {
    OauthToken getTokenMB(String clientMessageId);
}
