package com.stg.service3rd.common.logger;

import com.stg.common.Jackson;
import com.stg.service3rd.common.dto.CallerInfo;
import com.stg.service3rd.common.utils.LogUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.stg.service3rd.common.Constants.HeaderKey.CLIENT_MESSAGE_ID;
import static com.stg.service3rd.common.Constants.HeaderKey.CLIENT_REQUEST_ID;
import static com.stg.service3rd.common.Constants.HeaderKey.X_REQUEST_ID;
import static com.stg.service3rd.common.utils.ApiUtil.getFirstHeader;


@Slf4j
@Service
@RequiredArgsConstructor
public class ThirdPartyLogServiceImpl implements ThirdPartyLogService {
    private final ThirdPartyLogRepository thirdPartyLogRepository;
    private final Jackson jackson;

    @Override
    @Async
    public void log(CallerInfo callerInfo,
                    HttpEntity<?> request,
                    ResponseEntity<?> response,
                    LocalDateTime startTime,
                    LocalDateTime endTime,
                    @Nullable Exception reqException) {
        String requestId = getFirstHeader(request.getHeaders(), CLIENT_REQUEST_ID, UUID.randomUUID().toString());
        try {
            ThirdPartyLog thirdPartyLog = new ThirdPartyLog();
            thirdPartyLog.setRequestId(requestId);
            thirdPartyLog.setXRequestId(findThirdPartyRequestId(request));

            thirdPartyLog.setPath(callerInfo.getUrl());
            thirdPartyLog.setHostParty(callerInfo.getHostParty());
            thirdPartyLog.setMethod(callerInfo.getMethodName());
            thirdPartyLog.setAction(callerInfo.getFunc().getName());
            thirdPartyLog.setPayload(parseJsonb(request.getBody()));

            if (reqException != null) {
                if (reqException instanceof RestClientResponseException) {
                    RestClientResponseException responseException = (RestClientResponseException) reqException;
                    thirdPartyLog.setCode(responseException.getRawStatusCode());
                    thirdPartyLog.setErrorMessage(responseException.getResponseBodyAsString());
                } else if (reqException instanceof ResourceAccessException) {
                    // timeout,...
                    thirdPartyLog.setCode(HttpStatus.REQUEST_TIMEOUT.value());
                    thirdPartyLog.setErrorMessage(reqException.getMessage());
                } else {
                    thirdPartyLog.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    thirdPartyLog.setErrorMessage(reqException.getMessage());
                }
                thirdPartyLog.setErrorStack(LogUtil.stacksToJsonString(reqException));
            }
            if (response != null) {
                thirdPartyLog.setCode(response.getStatusCodeValue());
                thirdPartyLog.setResponse(parseJsonb(response.getBody()));
            }

            thirdPartyLog.setSendTime(startTime);
            thirdPartyLog.setReceivedTime(endTime);
            Duration duration = Duration.between(startTime, endTime);
            thirdPartyLog.setTotalTime((int) duration.toMillis());

            thirdPartyLogRepository.saveAndFlush(thirdPartyLog);
        } catch (Exception ex) {
            log.error("Error while save third-party-log, requestId=" + requestId, ex);
        }
    }


    private String findThirdPartyRequestId(HttpEntity<?> request) {
        return getFirstHeader(request.getHeaders(), X_REQUEST_ID, getFirstHeader(request.getHeaders(), CLIENT_MESSAGE_ID));
    }


    private String parseJsonb(Object jsonb) {
        if (jsonb == null) return null;

        try {
            return jackson.toJsonNonTrace(jsonb);
        } catch (Exception ex) {
            log.warn("Cannot convert to JSON, detail={}", ex.getMessage());
            return "{ \"noparse\": \"" + jsonb.toString() + "\" }";
        }
    }
}
