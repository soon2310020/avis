package com.stg.service3rd.mbalv2.adapter;

import com.stg.service3rd.common.adapter.soap.Soap3rdCaller;
import com.stg.service3rd.common.dto.Host3rd;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import static com.stg.utils.Constants.HOST_PARTY;


@Service
public class MbalV2ApiCaller extends Soap3rdCaller {
    @Value("${api3rd.mbal_v2.client_id}")
    private String clientId;

    @Value("${api3rd.mbal_v2.client_secret}")
    private String clientSecret;

    public MbalV2ApiCaller(@Value("${api3rd.mbal_v2.host}") @NonNull String mbalHost) {
        super(new Host3rd(mbalHost, HOST_PARTY.MBAL));
    }

    @Override
    public HttpHeaders getAuthHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.set("SOAPAction", "http://sap.com/xi/WebService/soap1.1");
        return headers;
    }
}
