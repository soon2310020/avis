package com.stg.service3rd.hcm.adapter;

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
public class HcmApiCaller extends Api3rdCaller implements IApi3rdMethods {


    public HcmApiCaller(@Value("${api3rd.hcm.host}") @NonNull String hcmHost) {
        super(new Host3rd(hcmHost, HOST_PARTY.HCM));
    }


    @Override
    public HttpHeaders getAuthHeader() {
        return new HttpHeaders();
    }

}
