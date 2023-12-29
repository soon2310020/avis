package com.stg.service3rd.mbal.adapter;

import com.stg.service3rd.common.adapter.Api3rdCaller;
import com.stg.service3rd.common.adapter.IApi3rdMethods;
import com.stg.service3rd.common.dto.Host3rd;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import static com.stg.utils.Constants.HOST_PARTY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Service
public class MbalApiCaller extends Api3rdCaller implements IApi3rdMethods {
    @Value("${api3rd.mbal.client_id}")
    private String clientIdUl3;

    @Value("${api3rd.mbal.client_secret}")
    private String clientSecretUl3;

    public MbalApiCaller(@Value("${api3rd.mbal.host}") @NonNull String mbalHost) {
        super(new Host3rd(mbalHost, HOST_PARTY.MBAL));
    }


    @Override
    public HttpHeaders getAuthHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientIdUl3, clientSecretUl3);
        headers.setContentType(APPLICATION_JSON);
        return headers;
    }

}
