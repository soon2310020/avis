package com.stg.service.impl;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stg.controller.aspect.RequestLogAspect;
import com.stg.entity.RequestLog;
import com.stg.repository.RequestLogRepository;
import com.stg.service.RequestLogService;
import com.stg.service3rd.common.utils.LogUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RequestLogServiceImpl implements RequestLogService {

    @Autowired
    private RequestLogRepository requestLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void log(HttpServletRequest request, ResponseEntity<?> response, LocalDateTime startTime,
            LocalDateTime endTime) {

        String requestId = (String) request.getAttribute(RequestLogAspect.ATTR_REQUEST_ID);
        try {
            RequestLog log = new RequestLog();

            log.setRequestId(requestId);

            String url = request.getRequestURL()
                    + (StringUtils.hasText(request.getQueryString()) ? "?" + request.getQueryString() : "");
            log.setUrl(url);

            String method = request.getMethod();
            log.setMethod(method);

            if (MediaType.APPLICATION_JSON_VALUE.equals(request.getContentType())) {
                String payload = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                log.setPayload(payload);
            }

            if (response != null) {
                Integer statusCode = response.getStatusCode().value();
                log.setCode(statusCode);

                Object body = response.getBody();
                if (body != null) {
                    String resp = objectMapper.writeValueAsString(response.getBody());
                    log.setResponse(resp);

                    Exception exception = (Exception) request.getAttribute(RequestLogAspect.ATTR_REQUEST_ERROR);

                    if (exception != null) {
                        String errorStack = LogUtil.stacksToJsonString(exception);
                        log.setErrorMessage(resp);
                        log.setErrorStack(errorStack);
                    }

                }
            }

            log.setRequestTime(startTime);
            log.setResponseTime(endTime);

            requestLogRepository.save(log);
        } catch (Exception e) {
            log.error("Error while save request-log, requestId=" + requestId, e);
        }

    }

}
