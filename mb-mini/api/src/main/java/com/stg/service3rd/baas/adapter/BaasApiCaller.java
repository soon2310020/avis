package com.stg.service3rd.baas.adapter;

import com.stg.errors.ApplicationException;
import com.stg.service.BaasService;
import com.stg.service.dto.baas.OauthToken;
import com.stg.service3rd.baas.adapter.property.AutoDebitProperties;
import com.stg.service3rd.common.adapter.Api3rdCaller;
import com.stg.service3rd.common.adapter.IApi3rdMethods;
import com.stg.service3rd.common.dto.Host3rd;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.UUID;

import static com.stg.service3rd.common.Constants.HeaderKey.AUTHORIZATION;
import static com.stg.service3rd.common.Constants.HeaderKey.BEARER;
import static com.stg.service3rd.common.Constants.HeaderKey.CLIENT_CREDENTIALS;
import static com.stg.service3rd.common.Constants.HeaderKey.CLIENT_MESSAGE_ID;
import static com.stg.service3rd.common.Constants.HeaderKey.GRANT_TYPE;
import static com.stg.utils.CommonMessageError.MSG12;
import static com.stg.utils.Constants.HOST_PARTY;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class BaasApiCaller extends Api3rdCaller implements IApi3rdMethods {
    private final AutoDebitProperties autoDebitProperties;

    @Autowired
    private BaasService baasService;

    @Autowired
    public BaasApiCaller(AutoDebitProperties autoDebitProperties) {
        super(new Host3rd(autoDebitProperties.getHost(), HOST_PARTY.BAAS));
        this.autoDebitProperties = autoDebitProperties;
    }


    /***/
    @Override
    public HttpHeaders getAuthHeader() {
        String clientMessageId = UUID.randomUUID().toString();

        OauthToken oauthToken = baasService.generateToken();
        if (oauthToken == null) {
            log.error("[MB]--Co loi khi get access-token in clientMessageId {}", clientMessageId);
            throw new ApplicationException(MSG12);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, BEARER + oauthToken.getAccessToken());
        headers.add(CLIENT_MESSAGE_ID, clientMessageId);
        headers.setContentType(APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    /***/
    private OauthToken getAccessToken(String clientMessageId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(autoDebitProperties.getClientId(), autoDebitProperties.getClientSecret());
        headers.add(CLIENT_MESSAGE_ID, clientMessageId);
        headers.setContentType(APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formRequest = new LinkedMultiValueMap<>();
        formRequest.add(GRANT_TYPE, CLIENT_CREDENTIALS);

        try {
            return this.post(BaasFunctions.GET_ACCESS_TOKEN, headers, formRequest, OauthToken.class);
        } catch (Exception e) {
            log.error("[MB]--Can not get access-token, detail: {}", e.getMessage());
            return null;
        }
    }
}
