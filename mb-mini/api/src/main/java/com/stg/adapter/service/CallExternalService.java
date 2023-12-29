package com.stg.adapter.service;

import com.stg.errors.BaasApiException;
import com.stg.service.impl.AsyncObjectImpl;
import com.stg.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static com.stg.utils.CommonMessageError.INSTALLMENT_MSG35;

@Service
@Slf4j
@RequiredArgsConstructor
public class CallExternalService implements CallExternalIService {
    private final RestTemplate restTemplate;
    private final AsyncObjectImpl asyncObject;

    @Override
    public <T> T callExternal(String host, HttpEntity<?> entity, Class<T> clazz, String payloadLog, Constants.HOST_PARTY hostParty) {
        LocalDateTime sendTime = LocalDateTime.now();
        try {
            ResponseEntity<?> exchange = restTemplate.exchange(host, HttpMethod.POST, entity, clazz);
            return clazz.cast(exchange.getBody());
        } catch (Exception e) {
            log.error("[BAAS]--Call to API error {}, detail {}", host, e.getMessage());
            LocalDateTime receivedTime = LocalDateTime.now();
            asyncObject.saveErrorLog(hostParty, HttpMethod.POST, host, payloadLog, 500, e.getMessage(), sendTime, receivedTime);
            throw new BaasApiException(INSTALLMENT_MSG35);
        }
    }
}
