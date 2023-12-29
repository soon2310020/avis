package com.stg.service3rd.ocr.adapter;

import com.stg.errors.ApplicationException;
import com.stg.service.dto.baas.OauthToken;
import com.stg.service3rd.common.adapter.Api3rdCaller;
import com.stg.service3rd.common.adapter.IApi3rdMethods;
import com.stg.service3rd.common.dto.Host3rd;
import com.stg.service3rd.ocr.exception.OCRApi3rdException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static com.stg.constant.CommonMessageError.MSG12;
import static com.stg.service3rd.common.Constants.HeaderKey.BEARER;
import static com.stg.service3rd.common.Constants.HostParty;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

@Slf4j
@Service
public class OcrKeycloakApiCaller extends Api3rdCaller implements IApi3rdMethods {
    public static final String AUTHORIZATION = "Authorization";
    @Value("${api3rd.ocr.keycloak.client_id}")
    private String clientId;
    @Value("${api3rd.ocr.keycloak.username}")
    private String username;
    @Value("${api3rd.ocr.keycloak.password}")
    private String password;




    public OcrKeycloakApiCaller(@Value("${api3rd.ocr.keycloak.host}") @NonNull String ocrKeycloakHost) {
        super(new Host3rd(ocrKeycloakHost, HostParty.OCR));
    }


    @Override
    public HttpHeaders getAuthHeader() {
        try {
            OauthToken ocrToken  = this.getAccessToken();
            if (ocrToken == null) {
                log.error("[OCR]--Co loi khi get ocrAccessToken ");
                throw new ApplicationException(MSG12);
            }
            HttpHeaders headers = new HttpHeaders();
            headers.add(AUTHORIZATION, BEARER + ocrToken.getAccessToken());
            return headers;
        } catch (Exception e) {
            throw new OCRApi3rdException(e.getMessage(),e);
        }
    }
    public OauthToken getAccessToken() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("grant_type", "password");
        request.add("username", username);
        request.add("password", password);
        request.add("client_id",clientId);
        request.add("Scope-Authorization","ecm-ocr-coor-write");
        OauthToken oauthToken =  this.post(OcrFunctions.GET_TOKEN, headers, request, OauthToken.class);
        if (oauthToken == null) {
            log.error("[OCR]--Can not get token from OCR");
            return null;
        }
        return oauthToken;
    }


}
