package com.stg.service3rd.hcm.adapter;

import com.stg.errors.ApplicationException;
import com.stg.service3rd.common.adapter.Api3rdCaller;
import com.stg.service3rd.common.adapter.IApi3rdMethods;
import com.stg.service3rd.common.dto.Host3rd;
import com.stg.service3rd.hcm.dto.resp.OauthToken;
import com.stg.service3rd.hcm.exception.HcmApiRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static com.stg.service3rd.common.Constants.HeaderKey.AUTHORIZATION;
import static com.stg.service3rd.common.Constants.HeaderKey.BEARER;
import static com.stg.utils.Constants.HOST_PARTY;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Service
public class HcmAuthApiCaller extends Api3rdCaller implements IApi3rdMethods {
    @Value("${api3rd.hcm.auth.client_id}")
    private String clientIdUl3;

    @Value("${api3rd.hcm.auth.client_secret}")
    private String clientSecretUl3;

    @Value("${api3rd.hcm.auth.scope}")
    private String scope;

    public HcmAuthApiCaller(@Value("${api3rd.hcm.auth.host}") @NonNull String hcmAuthHost) {
        super(new Host3rd(hcmAuthHost, HOST_PARTY.HCM));
    }


    @Override
    public HttpHeaders getAuthHeader()  {

        try {
           OauthToken hcmToken  = this.getAccessToken();
            HttpHeaders headers = new HttpHeaders();
            if (hcmToken == null) {
                log.error("[HCM]--Co loi khi get ocrAccessToken ");
                throw new HcmApiRequestException("Cant get access token from HCM");
            }
            headers.add(AUTHORIZATION, BEARER + hcmToken.getAccessToken());
            return headers;
        } catch (Exception e) {
            throw new HcmApiRequestException(e.getMessage(),e);
        }
    }

    private OauthToken getAccessToken() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("grant_type", "client_credentials");
        request.add("client_secret", clientSecretUl3);
        request.add("scope", scope);
        request.add("client_id",clientIdUl3);

       OauthToken oauthToken = this.post(HcmFunctions.GET_TOKEN, headers,request, OauthToken.class);
        if (oauthToken == null) {
            log.error("[HCM]--Can not get token from HCM");
            return null;
        }
        return oauthToken;
    }
}
