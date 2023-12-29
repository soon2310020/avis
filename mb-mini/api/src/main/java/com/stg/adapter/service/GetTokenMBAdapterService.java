package com.stg.adapter.service;

import com.stg.errors.MiniApiException;
import com.stg.service.dto.baas.OauthToken;
import com.stg.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static com.stg.utils.CommonMessageError.MSG12;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetTokenMBAdapterService implements GetTokenMBAdapterIService {
    private static final String AUTHORIZATION = "Authorization";
    private static final String CLIENT_MESSAGE_ID = "clientMessageId";
    private static final String GRANT_TYPE = "grant_type";
    private static final String CLIENT_CREDENTIALS = "client_credentials";

    @Value("${external.installment-key.client_id}")
    private String installmentClientId;
    @Value("${external.installment-key.client_secret}")
    private String installmentClientSecret;
    @Value("${external.host.installment_host_token}")
    private String installmentHost;
    
    private final CallExternalIService callExternalIService;

    @Override
    public OauthToken getTokenMB(String clientMessageId) {
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setBasicAuth(installmentClientId, installmentClientSecret);
        tokenHeaders.add(CLIENT_MESSAGE_ID, clientMessageId);
        tokenHeaders.setContentType(APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
        tokenRequest.add(GRANT_TYPE, CLIENT_CREDENTIALS);
        HttpEntity<MultiValueMap<String, String>> tokenRequestEntity = new HttpEntity<>(tokenRequest, tokenHeaders);
        OauthToken oauthToken = callExternalIService.callExternal(installmentHost + "/oauth2/v1/token", tokenRequestEntity, OauthToken.class, "", Constants.HOST_PARTY.MB);
        if (oauthToken == null) {
            log.error("[CRM]--Can not get token from MB");
            throw new MiniApiException(MSG12);
        }
        return oauthToken;
    }
}
