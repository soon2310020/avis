package com.stg.service3rd.mic_sandbox.adapter;

import com.stg.service.dto.external.request.MicGetTokenReqDto;
import com.stg.service3rd.common.adapter.Api3rdCaller;
import com.stg.service3rd.common.adapter.IApi3rdMethods;
import com.stg.service3rd.common.dto.Host3rd;
import com.stg.service3rd.mic_sandbox.adapter.property.MicSandboxProperties;
import com.stg.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import static org.springframework.http.MediaType.APPLICATION_JSON;


@Slf4j
@Service
public class MicSandboxApiCaller extends Api3rdCaller implements IApi3rdMethods {

    private final MicSandboxProperties micSandboxProperties;

    public MicSandboxApiCaller(@Value("${external.host.mic-host-sandbox}") @NonNull String micSandboxHost,
                               MicSandboxProperties micSandboxProperties) {
        super(new Host3rd(micSandboxHost, Constants.HOST_PARTY.MIC));
        this.micSandboxProperties = micSandboxProperties;
    }

    @Override
    public HttpHeaders getAuthHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        return headers;
    }

    public MicGetTokenReqDto genMicGetTokenReq() {
        return new MicGetTokenReqDto()
                .setClientId(micSandboxProperties.getClientId())
                .setPassword(micSandboxProperties.getPassword())
                .setClientSecret(micSandboxProperties.getClientSecret())
                .setUsername(micSandboxProperties.getUsername());
    }
}
