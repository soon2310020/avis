package com.stg.adapter.service;

import com.google.gson.Gson;
import com.stg.adapter.dto.CommonAdapterResponse;
import com.stg.service.dto.baas.OauthToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static com.stg.utils.Common.generateUUIDId;

@Service
@Slf4j
@RequiredArgsConstructor
public class CallCardPartnerAdapterService implements CallCardPartnerAdapterIService {
    private final GetTokenMBAdapterIService getTokenCrmIAdapterService;
    private final RestTemplate restTemplate;
    private final Gson gson;

    @Override
    public <P> CommonAdapterResponse callGetCardPartnerAdapter(String urlTemplate, Map<String, String> params, HttpMethod method, HttpHeaders headers, P param) {
        log.debug("start call card partner -----------> ");
        String clientMessageId = generateUUIDId(20);
        OauthToken crmToken = getTokenCrmIAdapterService.getTokenMB(clientMessageId);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + crmToken.getAccessToken());
        headers.set("clientMessageId", clientMessageId);
        HttpEntity<?> entity  = new HttpEntity<>(param, headers);
        log.info("[MINI]-- call template {} with clientMessageId: {}, header: ***, params: {}, param: {}", urlTemplate, clientMessageId, params, param);
        try {
            ResponseEntity<String> response = restTemplate.exchange(urlTemplate, method, entity, String.class, params);
            CommonAdapterResponse commonAdapterResponse = gson.fromJson(response.getBody(), CommonAdapterResponse.class);
            log.info("[INSTALLMENT]--end call card partner success ---------->. Response {}", commonAdapterResponse);
            return commonAdapterResponse;
        } catch (HttpStatusCodeException e) {
            try {
                CommonAdapterResponse commonAdapterResponse = gson.fromJson(e.getResponseBodyAsString(), CommonAdapterResponse.class);
                log.error("[INSTALLMENT]--end call card partner error {} ------------", commonAdapterResponse);
                return commonAdapterResponse;
            }catch (Exception ex){
                log.error("[INSTALLMENT]--end no parse response call card partner {} ---------",ex.getMessage());
                return new CommonAdapterResponse(clientMessageId,"500");
            }
        }catch (Exception ex){
            log.error("[INSTALLMENT]--end call card partner error {} ---------",ex.getMessage());
            return new CommonAdapterResponse(clientMessageId,"500");
        }
    }
}
