package com.emoldino.framework.aspect;

import com.emoldino.framework.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ServiceExceptionHandler {

    @Around("execution(* saleson.api.part.*Service.save(..))" +
        " or execution(* saleson.api.mold.*Service.save(..))")
    public Object handleException(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (DataIntegrityViolationException ex) {
            String message = ex.getRootCause().getMessage();
            if (StringUtils.isBlank(message) || !message.startsWith("Data truncation:")) throw ex;
            message = message.replaceAll(" at row (\\d+)?$", "");
            throw new BizException(message, ex);
        } catch (Exception ex) {
            throw ex;
        }
    }
}
