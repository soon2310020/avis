package com.stg.service3rd.common.adapter.soap;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.stg.errors.dto.ErrorDto;
import com.stg.service3rd.common.adapter.IApi3rdConfig;
import com.stg.service3rd.common.adapter.func.IFunction;
import com.stg.service3rd.common.dto.CallerInfo;
import com.stg.service3rd.common.dto.Host3rd;
import com.stg.service3rd.common.dto.soap.EResponse;
import com.stg.service3rd.common.dto.soap.SoapRequest;
import com.stg.service3rd.common.dto.soap.SoapResponse;
import com.stg.service3rd.common.dto.soap.SoapResponseBody;
import com.stg.service3rd.common.dto.soap.SoapResponseError;
import com.stg.service3rd.common.exception.Api3rdException;
import com.stg.service3rd.common.logger.ThirdPartyLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.stg.service3rd.common.Constants.HeaderKey.CLIENT_REQUEST_ID;

/**
 * for: soap api - MBAL
 * RestTemplate => HttpStatus(4xx,5xx,...) != success => always throw exception
 */
public abstract class Soap3rdCaller implements IApi3rdConfig, ISoap3rdMethods {
    private final Logger logger;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ThirdPartyLogService thirdPartyLogService;

    protected final Host3rd host3rd;

    private final XmlMapper xmlMapper;

    protected Soap3rdCaller(Host3rd host3rd) {
        this.host3rd = host3rd;
        this.logger = LoggerFactory.getLogger(this.getClass()); /* Singleton from implementation class (top class) */
        this.xmlMapper = new XmlMapper();
    }

    @Override
    public final Host3rd getHost3rd() {
        return this.host3rd;
    }

    /***/
    @Override
    public <B extends SoapResponseBody, R extends SoapResponse<B>> R post(IFunction func, HttpHeaders headers, SoapRequest payload, Class<R> responseType) throws Exception {
        LocalDateTime startTime = LocalDateTime.now();
        String url = this.host3rd.getDomain() + func.getUri();
        logger.info("[{}][POST]--[START] :: {}, action={}", this.host3rd.getName(), url, func.getName());
        String clientRequestId = UUID.randomUUID().toString();

        String requestBody = null;
        Exception exception = null;
        HttpEntity<String> requestEntity = null;
        ResponseEntity<String> exchange = null;
        try {
            requestBody = xmlMapper.writeValueAsString(payload);
            headers.set(CLIENT_REQUEST_ID, clientRequestId); // tracking: request by this server!
            requestEntity = new HttpEntity<>(requestBody, headers);

            exchange = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            R response = xmlMapper.readValue(exchange.getBody(), responseType);

            EResponse responseStatus = response.getBody().getMessage();
            if ("000".equals(responseStatus.getCode())) {
                return response;
            } else {
                throw new Api3rdException(responseStatus.getMessageEn(), new ErrorDto(exchange.getStatusCodeValue(), responseStatus.getCode(), responseStatus.getMessageVi()));
            }
        } catch (Exception ex) {
            exception = ex;
            if (ex instanceof RestClientResponseException) {
                RestClientResponseException clientRespException = (RestClientResponseException) ex;
                String errorBody = clientRespException.getResponseBodyAsString();
                SoapResponseError soapResponseError = xmlMapper.readValue(errorBody, SoapResponseError.class);
                String errorMessage = soapResponseError.getBody().getFault().getDetail().getMessage();
                throw new Api3rdException(errorMessage, new ErrorDto(HttpStatus.valueOf(clientRespException.getRawStatusCode()), errorMessage));
            } else {
                throw ex;
            }
        } finally {
            LocalDateTime endTime = LocalDateTime.now();
            if (func.hasSaveLog()) {
                CallerInfo callerInfo = CallerInfo.of(this.host3rd.getName(), url, HttpMethod.POST, func);
                thirdPartyLogService.log(callerInfo, requestEntity, exchange, startTime, endTime, exception);
            }

            Duration duration = Duration.between(startTime, endTime);
            if (exception != null) {
                String errorDetail = "detail=" + exception.getMessage() + ", requestData=" + requestBody;
                logger.error("[{}][POST]--[ERROR] :: {}, action={}, requestId={}, responseTime={}, {}", this.host3rd.getName(), url, func.getName(), clientRequestId, duration.toMillis(), errorDetail);
            } else {
                logger.info("[{}][POST]--[DONE ] :: {}, action={}, requestId={}, responseTime={}", this.host3rd.getName(), url, func.getName(), clientRequestId, duration.toMillis());
            }
        }
    }

    @Override
    public <B extends SoapResponseBody, R extends SoapResponse<B>> R post(IFunction func, SoapRequest payload, Class<R> responseType) throws Exception {
        return this.post(func, this.getAuthHeader(), payload, responseType);
    }
}
