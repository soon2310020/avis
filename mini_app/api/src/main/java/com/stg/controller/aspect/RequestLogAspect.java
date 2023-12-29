package com.stg.controller.aspect;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.stg.service.RequestLogService;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class RequestLogAspect {

    public static final String ATTR_REQUEST_TIME = "REQUEST_TIME";
    public static final String ATTR_REQUEST_ID = "REQUEST_ID";
    public static final String ATTR_REQUEST_ERROR = "REQUEST_ERROR";
    public static final String ATTR_REQUEST_LOG = "REQUEST_LOG";
    public static final String HEADER_REQUEST_ID = "CX-Request-Id";

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Autowired
    private RequestLogService requestLogService;

    @Before("@annotation(RequestLog)")
    public void before() {
        if (request.getAttribute(ATTR_REQUEST_TIME) == null) {
            request.setAttribute(ATTR_REQUEST_TIME, LocalDateTime.now());
        }

        if (request.getHeader(HEADER_REQUEST_ID) != null) {
            request.setAttribute(ATTR_REQUEST_ID, request.getHeader(HEADER_REQUEST_ID));
        } else {
            request.setAttribute(ATTR_REQUEST_ID, UUID.randomUUID().toString());
        }

        if (request.getAttribute(ATTR_REQUEST_LOG) == null) {
            String requestId = (String) request.getAttribute(ATTR_REQUEST_ID);
            log.info("[REQUEST-LOG]--[{}] :: {}, requestId={}", request.getMethod(), request.getRequestURL(), requestId);
            request.setAttribute(ATTR_REQUEST_LOG, Boolean.TRUE);
        }

    }

    @AfterReturning(pointcut = "@annotation(RequestLog) || @annotation(org.springframework.web.bind.annotation.ExceptionHandler)", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) throws Throwable {

        if (request.getAttribute(ATTR_REQUEST_LOG) != Boolean.TRUE) {
            return;
        }

        String requestId = (String) request.getAttribute(ATTR_REQUEST_ID);
        ResponseEntity<?> responseEntity = null;
        if (result instanceof ResponseEntity) {
            responseEntity = (ResponseEntity<?>) result;
        }
        requestLogService.log(request, responseEntity, (LocalDateTime) request.getAttribute(ATTR_REQUEST_TIME), LocalDateTime.now());
        response.setHeader(HEADER_REQUEST_ID, requestId);
    }

    @AfterThrowing(pointcut = "@annotation(RequestLog)", throwing = "error")
    public void afterThrowing(JoinPoint joinPoint, Throwable error) throws Throwable {
        request.setAttribute(ATTR_REQUEST_ERROR, error);
    }

}
