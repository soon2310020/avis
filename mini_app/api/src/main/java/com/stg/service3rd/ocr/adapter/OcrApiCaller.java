package com.stg.service3rd.ocr.adapter;

import com.stg.errors.ApplicationException;
import com.stg.service.dto.baas.OauthToken;
import com.stg.service3rd.common.adapter.Api3rdCaller;
import com.stg.service3rd.common.adapter.IApi3rdMethods;
import com.stg.service3rd.common.dto.Host3rd;
import com.stg.service3rd.ocr.exception.OCRApi3rdException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import static com.stg.constant.CommonMessageError.MSG12;
import static com.stg.service3rd.common.Constants.HeaderKey.BEARER;
import static com.stg.service3rd.common.Constants.HostParty;

@Slf4j
@Service
public class OcrApiCaller extends Api3rdCaller implements IApi3rdMethods {

    private final OcrKeycloakApiCaller ocrKeycloakApiCaller;
    public static final String AUTHORIZATION = "Authorization";
    @Autowired
    public OcrApiCaller(@Value("${api3rd.ocr.host}") @NonNull String ocrHost, OcrKeycloakApiCaller ocrKeycloakApiCaller) {
        super(new Host3rd(ocrHost, HostParty.OCR));
        this.ocrKeycloakApiCaller = ocrKeycloakApiCaller;
    }


    @Override
    public HttpHeaders getAuthHeader() {
        try {
            OauthToken ocrToken  = ocrKeycloakApiCaller.getAccessToken();
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
}
