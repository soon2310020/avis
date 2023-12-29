package com.stg.service3rd.mic.adapter;

import com.stg.service3rd.common.Constants;
import com.stg.service3rd.common.adapter.Api3rdCaller;
import com.stg.service3rd.common.adapter.IApi3rdMethods;
import com.stg.service3rd.common.dto.Host3rd;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Service
public class MicSandboxApiCaller extends Api3rdCaller implements IApi3rdMethods {

    public MicSandboxApiCaller(MicSandboxProperties micSandboxProperties) {
        super(new Host3rd(micSandboxProperties.getHost(), Constants.HostParty.MIC));
        //this.micSandboxProperties = micSandboxProperties;
    }


    @Override
    public HttpHeaders getAuthHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        return headers;
    }
}
