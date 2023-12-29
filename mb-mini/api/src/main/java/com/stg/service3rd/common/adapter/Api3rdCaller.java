package com.stg.service3rd.common.adapter;

import com.stg.config.jackson.Jackson;
import com.stg.service3rd.common.adapter.func.IFunction;
import com.stg.service3rd.common.dto.CallerInfo;
import com.stg.service3rd.common.dto.Host3rd;
import com.stg.service3rd.common.logger.ThirdPartyLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static com.stg.service3rd.common.Constants.HeaderKey.CLIENT_REQUEST_ID;

/*
 * NOTE: exchange & exception không đồng thời != null! (1 cái null, cái còn lại khác null)
 * */
public abstract class Api3rdCaller implements IApi3rdConfig, IApi3rdMethods, IApi3rdSimpleMethods {
    private final Logger logger;

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private Jackson jackson;
    @Autowired
    private ThirdPartyLogService thirdPartyLogService;
    /***/
    protected final Host3rd host3rd;

    protected Api3rdCaller(Host3rd host3rd) {
        this.host3rd = host3rd;
        this.logger = LoggerFactory.getLogger(this.getClass()); /*Singleton from implementation class (top class)*/
    }

    @Override
    public final Host3rd getHost3rd() {
        return this.host3rd;
    }


    /***/
    @Override
    public <B> B post(IFunction func, HttpHeaders headers, Object payload, Class<B> responseType) throws Exception {
        LocalDateTime startTime = LocalDateTime.now();

        String url = this.host3rd.getDomain() + func.getUri();
        String clientRequestId = UUID.randomUUID().toString();
        headers.set(CLIENT_REQUEST_ID, clientRequestId); // request by this server!
        HttpEntity<?> requestEntity = new HttpEntity<>(payload, headers);
        logger.info("[{}][POST]--[START] :: {}, action={}, requestId={}", this.host3rd.getName(), url, func.getName(), clientRequestId);

        Exception exception = null;
        ResponseEntity<B> exchange = null;
        try {
            exchange = restTemplate.exchange(url, HttpMethod.POST, requestEntity, responseType);
            return exchange.getBody();
        } catch (Exception ex) {
            exception = ex;
            throw ex;
        } finally {
            LocalDateTime endTime = LocalDateTime.now();
            if (func.hasSaveLog()) {
                CallerInfo callerInfo = CallerInfo.of(this.host3rd.getName(), url, HttpMethod.POST, func);
                thirdPartyLogService.log(callerInfo, requestEntity, exchange, startTime, endTime, exception);
            }

            Duration duration = Duration.between(startTime, endTime);
            if (exception != null) {
                String errorDetail = "detail=" + exception.getMessage() + ", requestData=" + jackson.toString(payload);
                logger.error("[{}][POST]--[ERROR] :: {}, action={}, requestId={}, responseTime={}, {}", this.host3rd.getName(), url, func.getName(), clientRequestId, duration.toMillis(), errorDetail);
            } else {
                logger.info("[{}][POST]--[DONE ] :: {}, action={}, requestId={}, responseTime={}", this.host3rd.getName(), url, func.getName(), clientRequestId, duration.toMillis());
            }
        }
    }

    /***/
    @Override
    public <B> B get(IFunction func, HttpHeaders headers, Object payload, Class<B> responseType, @Nullable Map<String, ?> uriVariables) throws Exception {
        LocalDateTime startTime = LocalDateTime.now();

        String url = this.host3rd.getDomain() + func.getUri();
        String clientRequestId = UUID.randomUUID().toString();
        headers.set(CLIENT_REQUEST_ID, clientRequestId); // request by this server!
        HttpEntity<?> requestEntity = new HttpEntity<>(payload, headers);
        logger.info("[{}][GET ]--[START] :: {}, action={}, requestId={}", this.host3rd.getName(), url, func.getName(), clientRequestId);

        Exception exception = null;
        ResponseEntity<B> exchange = null;
        try {
            if (uriVariables != null) {
                UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
                uriVariables.forEach(uriBuilder::queryParam);
                url = uriBuilder.encode().toUriString();
            }
            exchange = restTemplate.exchange(url, HttpMethod.GET, requestEntity, responseType);

            return exchange.getBody();
        } catch (Exception ex) {
            exception = ex;
            throw ex;
        } finally {
            LocalDateTime endTime = LocalDateTime.now();
            if (func.hasSaveLog()) {
                CallerInfo callerInfo = CallerInfo.of(this.host3rd.getName(), url, HttpMethod.GET, func);
                thirdPartyLogService.log(callerInfo, requestEntity, exchange, startTime, endTime, exception);
            }

            Duration duration = Duration.between(startTime, endTime);
            if (exception != null) {
                logger.error("[{}][GET ]--[ERROR] :: {}, action={}, requestId={}, responseTime={}, detail={}", this.host3rd.getName(), url, func.getName(), clientRequestId, duration.toMillis(), exception.getMessage());
            } else {
                logger.info("[{}][GET ]--[DONE ] :: {}, action={}, requestId={}, responseTime={}", this.host3rd.getName(), url, func.getName(), clientRequestId, duration.toMillis());
            }
        }
    }

    @Override
    public <B> B post(IFunction func, Object payload, Class<B> responseType) throws Exception {
        return this.post(func, this.getAuthHeader(), payload, responseType);
    }

    @Override
    public <B> B get(IFunction func, Object payload, Class<B> responseType, Map<String, ?> uriVariables) throws Exception {
        return this.get(func, this.getAuthHeader(), payload, responseType, uriVariables);
    }
}
