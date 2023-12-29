package com.stg.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.stg.errors.MBApiException;
import com.stg.errors.MbalApiException;
import com.stg.errors.MicApiException;
import com.stg.service.dto.external.request.MbalCustomerInfoReqDto;
import com.stg.service.dto.external.response.MbalGenAppNumberRespDto;
import com.stg.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static com.stg.utils.Common.generateMbalDefaultHeader;
import static com.stg.utils.CommonMessageError.*;
import static com.stg.utils.Endpoints.MBAL_GENERATE_APP_NUMBER;

@Service
@RequiredArgsConstructor
@Slf4j
public class BaseServiceAsync {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseServiceAsync.class);

    @Value("${external.host.mb-host}")
    private String mbHost;
    @Value("${external.host.mbal-host}")
    private String mbalHost;
    @Value("${external.host.mic-host}")
    private String micHost;

    @Autowired
    private RestTemplate restTemplate;

    private final AsyncObjectImpl asyncObject;

    @Async
    public CompletableFuture<MbalGenAppNumberRespDto> mbalGenAppNumber(String mbalAccessToken, MbalCustomerInfoReqDto reqDto) {
        long startTime = System.currentTimeMillis();
        HttpEntity<MbalCustomerInfoReqDto> entity = new HttpEntity<>(reqDto, generateMbalDefaultHeader(mbalAccessToken));

        String payloadLog = getPayloadLogObject(reqDto);
        CompletableFuture<MbalGenAppNumberRespDto> completableFuture = CompletableFuture.completedFuture(getResponseForPostRequest(mbalHost + MBAL_GENERATE_APP_NUMBER, entity, MbalGenAppNumberRespDto.class, payloadLog, Constants.HOST_PARTY.MBAL));
        LOGGER.info("Total time mbalGenAppNumber: {}", System.currentTimeMillis() - startTime);
        return completableFuture;
    }

    private static String getPayloadLogObject(Object reqDto) {
        try {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            return ow.writeValueAsString(reqDto);
        } catch (JsonProcessingException jsonProcessingException) {
            log.error("[MINI]--Lỗi khi convert error data to json {}", reqDto);
            return null;
        }
    }

    private <T> T getResponseForPostRequest(String host, HttpEntity<?> entity, Class<T> clazz, String payloadLog, Constants.HOST_PARTY hostParty) {
        LocalDateTime sendTime = LocalDateTime.now();
        try {
            ResponseEntity<?> exchange = restTemplate.exchange(host, HttpMethod.POST, entity, clazz);
            return clazz.cast(exchange.getBody());
        } catch (Exception e) {
            LOGGER.error("Call to API error {}, exception: {}", host, e.getMessage());
            LocalDateTime receivedTime = LocalDateTime.now();
            asyncObject.saveErrorLog(hostParty, HttpMethod.POST, host, payloadLog, 500, e.getMessage(), sendTime, receivedTime);
            if (host.contains(mbHost)) {
                throw new MBApiException(MSG12);
            } else if (host.contains(mbalHost)) {
                throw new MbalApiException(MSG12);
            }
            throw new MicApiException(MSG12);
        }
    }
}
