package vn.com.twendie.avis.mobile.api.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import vn.com.twendie.avis.api.core.ApplicationContextProvider;
import vn.com.twendie.avis.mobile.api.repository.SystemLogRepo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Slf4j
@Aspect
@Component
public class Logging {

    private SpelExpressionParser spelExpressionParser = new SpelExpressionParser();

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface MaskedParam {
        String maskedSpel();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface NoLogging {
    }

    @Pointcut("execution(public * *(..))")
    private void anyPublicOperation() {
    }

    @Pointcut("within(vn.com.twendie.avis.mobile.api.controller.*)")
    public void withinControllers() {
    }

    @Pointcut("within(vn.com.twendie.avis.mobile.api.service.impl.*)")
    public void withinServiceImpls() {
    }

    @Pointcut("within(vn.com.twendie.avis.mobile.api.repository.*)")
    public void withinRepoImpls() {
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.DeleteMapping) "
            + "|| @annotation(org.springframework.web.bind.annotation.GetMapping)"
            + "|| @annotation(org.springframework.web.bind.annotation.Mapping)"
            + "|| @annotation(org.springframework.web.bind.annotation.PatchMapping)"
            + "|| @annotation(org.springframework.web.bind.annotation.PostMapping)"
            + "|| @annotation(org.springframework.web.bind.annotation.PutMapping)"
            + "|| @annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void hasRequestMapping() {
    }

    @Pointcut("!@annotation(vn.com.twendie.avis.mobile.api.aop.Logging.NoLogging)")
    public void loggable() {
    }

    @Before("anyPublicOperation() && withinControllers() && hasRequestMapping() && loggable()")
    public void beforeControllersMappingMethod(JoinPoint joinPoint) {
        logBeforeMethodWithArgs(joinPoint);
    }

    @AfterReturning(value = "anyPublicOperation() && withinControllers() && hasRequestMapping() && loggable()", returning = "returnValue")
    public void afterControllersMappingMethod(JoinPoint joinPoint, Object returnValue) {
        logAfterMethod(joinPoint, returnValue);
    }

    @AfterThrowing(value = "anyPublicOperation() && withinControllers() && hasRequestMapping() && loggable()", throwing = "throwable")
    public void afterControllersMappingMethodThrow(JoinPoint joinPoint, Throwable throwable) {
        logAfterThrow(joinPoint, throwable);
    }

    @Before("anyPublicOperation() && withinServiceImpls() && loggable()")
    public void beforeServiceMethod(JoinPoint joinPoint) {
        logBeforeMethodWithArgs(joinPoint);
    }

    @AfterReturning(value = "anyPublicOperation() && withinServiceImpls() && loggable()", returning = "returnValue")
    public void afterServiceMethod(JoinPoint joinPoint, Object returnValue) {
        logAfterMethod(joinPoint, returnValue);
    }

    @AfterThrowing(value = "anyPublicOperation() && withinServiceImpls() && loggable()", throwing = "throwable")
    public void afterServiceThrow(JoinPoint joinPoint, Throwable throwable) {
        logAfterThrow(joinPoint, throwable);
    }

    @Before("anyPublicOperation() && withinRepoImpls() && loggable()")
    public void beforeRepoMethod(JoinPoint joinPoint) {
        logBeforeMethodWithArgs(joinPoint);
    }

    @AfterReturning(value = "anyPublicOperation() && withinRepoImpls() && loggable()", returning = "returnValue")
    public void afterRepoMethod(JoinPoint joinPoint, Object returnValue) {
        logAfterMethod(joinPoint, returnValue);
    }

    @AfterThrowing(value = "anyPublicOperation() && withinRepoImpls() && loggable()", throwing = "throwable")
    public void afterRepoMethodThrow(JoinPoint joinPoint, Throwable throwable) {
        logAfterThrow(joinPoint, throwable);
    }

    private void logBeforeMethodWithArgs(JoinPoint joinPoint) {
        StringBuilder msgBuilder = new StringBuilder("before ");
        LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();
        msgBuilder.append(jointPointName(joinPoint));
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String[] str = discoverer.getParameterNames(method);
        int paramsCount = method.getParameterCount();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < paramsCount; i++) {
            Parameter parameter = method.getParameters()[i];
            String paramName = str != null && str[i] != null ? str[i] : parameter.getName();
            msgBuilder.append(" ").append(paramName).append("=");
            MaskedParam maskedParam = parameter.getAnnotation(MaskedParam.class);
            if (maskedParam != null) {
                try {
                    msgBuilder.append("masked:")
                            .append(spelExpressionParser.parseRaw(maskedParam.maskedSpel()).getValue(args[i]));
                } catch (Exception e) {
                    // no-op
                }
            } else {
                // TODO: uncomment below code to stop logging user token
                msgBuilder.append(args[i]);
//                msgBuilder.append(paramName.matches("(?i).*token.*") ? "" : args[i]);
            }

        }
        log.info(msgBuilder.toString());

//        logTransactionDB("before",joinPoint);
    }

    private void logAfterMethod(JoinPoint joinPoint, Object returnValue) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            log.info("after {} return: {}", jointPointName(joinPoint),
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(returnValue));
        } catch (JsonProcessingException e) {
            log.info("after {} return: {}", jointPointName(joinPoint), returnValue);
        }
//        logTransactionDB("after",joinPoint);
    }

    private void logAfterThrow(JoinPoint joinPoint, Throwable throwable) {
        log.error("after exception method {} exp {}", jointPointName(joinPoint),
                ExceptionUtils.getRootCauseMessage(throwable));

//        logTransactionDB("afterThrow",joinPoint);
    }

    private String jointPointName(JoinPoint joinPoint) {
        return joinPoint.getSignature().toShortString();
    }

    @Around("anyPublicOperation() && withinServiceImpls() && loggable()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        Object proceed = joinPoint.proceed();

        long executionTime = System.currentTimeMillis() - start;

        System.out.println(joinPoint.getSignature() + " executed in " + executionTime + "ms");
        log.info("after {} executed in: {} ms", jointPointName(joinPoint), executionTime);
        return proceed;
    }

    private void logTransactionDB(String type, JoinPoint joinPoint) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            SystemLogRepo systemLogRepo = ApplicationContextProvider.getApplicationContext().getBean(SystemLogRepo.class);
            Long PS_CURRENT_THREAD_ID = systemLogRepo.findThread();

            log.info(type + " {} LOG_TRANSACTION PS_CURRENT_THREAD_ID: {}", jointPointName(joinPoint),
                    PS_CURRENT_THREAD_ID);
            Object[][] dataLock = systemLogRepo.getDataLocksCurrentThread();
            if (dataLock != null && dataLock.length > 0) {
                log.error(type + " {} LOG_TRANSACTION DATA_LOCKS: {}", jointPointName(joinPoint),
                        objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dataLock));
            }
        } catch (JsonProcessingException e) {
            log.info(type + " {} LOG_TRANSACTION return: {}", jointPointName(joinPoint), e.getMessage());
        }
    }

}
