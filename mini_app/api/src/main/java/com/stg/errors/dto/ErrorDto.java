package com.stg.errors.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

import java.time.Instant;


/**
 * Contains the common details that should be returned in case of an error during a web service request.
 */

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = ErrorDto.TYPE)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ErrorDto.class, name = ErrorDto.SIMPLE_TYPE)
})
@Slf4j
@Getter
@Setter
@NoArgsConstructor
public class ErrorDto {
    public static final String SIMPLE_TYPE = "simple";
    public static final String TYPE = "type";
    public static final String HTTP_STATUS_STR = "httpStatus={}";

    private String requestUid = MDC.get("X-B3-TraceId");
    private long timestamp = Instant.now().toEpochMilli();
    private int httpStatus;
    private String message;
    private String errorCode; // = "GeneralException" // don't show exception class name

    public ErrorDto(HttpStatus httpStatus, Exception e) {
        setHttpStatus(httpStatus.value());
        setMessage(e.getMessage());
        log.info(HTTP_STATUS_STR, httpStatus.value());
    }

    public ErrorDto(HttpStatus httpStatus, String message) {
        setHttpStatus(httpStatus.value());
        setMessage(message);
        log.info(HTTP_STATUS_STR, httpStatus.value());
    }

    public ErrorDto(HttpStatus httpStatus, Exception e, String errorCode) {
        setHttpStatus(httpStatus.value());
        setMessage(e.getMessage());
        setErrorCode(errorCode);
        log.info(HTTP_STATUS_STR, httpStatus.value());
    }

    public ErrorDto(int httpCode, String errorCode, String message) {
        setHttpStatus(httpCode);
        setErrorCode(errorCode);
        setMessage(message);

        log.info(HTTP_STATUS_STR, httpCode);
    }
}
