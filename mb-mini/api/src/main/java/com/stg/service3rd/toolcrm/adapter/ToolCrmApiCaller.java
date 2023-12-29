package com.stg.service3rd.toolcrm.adapter;

import com.stg.service3rd.common.adapter.Api3rdCaller;
import com.stg.service3rd.common.adapter.IApi3rdMethods;
import com.stg.service3rd.common.dto.Host3rd;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.stg.service3rd.common.Constants.CURRENT_SERVER_NAME;
import static com.stg.service3rd.common.Constants.HeaderKey.CLIENT_ID;
import static com.stg.service3rd.common.Constants.HeaderKey.X_AUTH_TOKEN;
import static com.stg.utils.Constants.HOST_PARTY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Service
public class ToolCrmApiCaller extends Api3rdCaller implements IApi3rdMethods {
    @Value("${api3rd.tool-crm.secret_key}")
    private String toolCrmSecretKey;

    public ToolCrmApiCaller(@Value("${api3rd.tool-crm.host}") @NonNull String toolCrmHost) {
        super(new Host3rd(toolCrmHost, HOST_PARTY.TOOL_CRM));
    }

    /***/
    @Override
    public HttpHeaders getAuthHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.setAccept(List.of(APPLICATION_JSON));
        headers.set(X_AUTH_TOKEN, toolCrmSecretKey);
        headers.set(CLIENT_ID, CURRENT_SERVER_NAME);
        return headers;
    }
}
