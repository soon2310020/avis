package com.stg.service3rd.crm.adapter;

import com.stg.errors.ApplicationException;
import com.stg.service.dto.baas.OauthToken;
import com.stg.service3rd.common.Constants;
import com.stg.service3rd.common.adapter.Api3rdCaller;
import com.stg.service3rd.common.adapter.IApi3rdMethods;
import com.stg.service3rd.common.dto.Host3rd;
import com.stg.service3rd.common.dto.error.IErrorObject;
import com.stg.service3rd.common.utils.ApiUtil;
import com.stg.service3rd.crm.exception.CrmApi3rdException;
import com.stg.service3rd.mbal.dto.error.MbalErrorObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static com.stg.service3rd.common.Constants.HeaderKey.BEARER;
import static com.stg.service3rd.common.Constants.HeaderKey.CLIENT_MESSAGE_ID;
import static com.stg.service3rd.mbal.constant.Common.generateUUIDId;
import static com.stg.constant.CommonMessageError.MSG12;
import static org.apache.http.client.config.AuthSchemes.BASIC;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Service
public class CrmApiCaller extends Api3rdCaller implements IApi3rdMethods {
    public static final String AUTHORIZATION = "Authorization";
    public static final String GRANT_TYPE = "grant_type";
    public static final String CLIENT_CREDENTIALS = "client_credentials";

    @Value("${api3rd.crm.client_id}")
    private String crmClientId;

    @Value("${api3rd.crm.client_secret}")
    private String crmClientSecret;

    @Value("${api3rd.crm.basic_value}")
    private String crmBasicValue;

    public CrmApiCaller(@Value("${api3rd.crm.host}") @NonNull String crmHost) {
        super(new Host3rd(crmHost, Constants.HostParty.CRM));
    }


    @Override
    public HttpHeaders getAuthHeader() {
        String clientMessageId = generateUUIDId(20);
        try {
            OauthToken crmToken = this.getAccessToken(clientMessageId);
            if (crmToken == null) {
                log.error("[CRM]--Co loi khi get crmAccessToken in clientMessageId {}", clientMessageId);
                throw new ApplicationException(MSG12);
            }
            HttpHeaders headers = new HttpHeaders();
            headers.add(AUTHORIZATION, BEARER + crmToken.getAccessToken());
            headers.add(CLIENT_MESSAGE_ID, clientMessageId);
            headers.setContentType(APPLICATION_JSON);
            return headers;
        } catch (Exception e) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(e, MbalErrorObject.class);
            throw new CrmApi3rdException(errorObject.getErrorMessage(), errorObject.toErrorDto());
        }
    }

    /***/
    private OauthToken getAccessToken(String clientMessageId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, BASIC + " " + crmBasicValue);
        headers.add(CLIENT_MESSAGE_ID, clientMessageId);
        headers.setContentType(APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formRequest = new LinkedMultiValueMap<>();
        formRequest.add(GRANT_TYPE, CLIENT_CREDENTIALS);
        formRequest.add("client_secret", crmClientSecret);
        formRequest.add("client_id", crmClientId);

        OauthToken oauthToken = this.post(CrmFunctions.GET_ACCESS_TOKEN, headers, formRequest, OauthToken.class);
        if (oauthToken == null) {
            log.error("[CRM]--Can not get token from CRM");
            return null;
        }
        return oauthToken;
    }
}
