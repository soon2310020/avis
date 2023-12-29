package com.stg.errors.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.stg.utils.DateUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

import static com.stg.utils.DateUtil.DATE_DMY_HMS;


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
@Getter
@Setter
@Slf4j
public class ErrorDto {
    public static final String SIMPLE_TYPE = "simple";
    public static final String TYPE = "type";
    public static final String HTTP_STATUS_STR = "httpStatus={}";

    private String requestUid = MDC.get("X-B3-TraceId");
    private String timestamp;
    private int httpStatus;
    private String message;
    private String errorCode = "GeneralException";

    public ErrorDto() {
        timestamp = DateUtil.localDateTimeToString(DATE_DMY_HMS, LocalDateTime.now());
    }

    public ErrorDto(HttpStatus httpStatus, Exception e) {
        this();
        setHttpStatus(httpStatus.value());
        setMessage(e.getMessage());
        log.info(HTTP_STATUS_STR, httpStatus.value());
    }

    public ErrorDto(HttpStatus httpStatus, String message) {
        this();
        setHttpStatus(httpStatus.value());
        setMessage(message);
        log.info(HTTP_STATUS_STR, httpStatus.value());
    }

    public ErrorDto(HttpStatus httpStatus, Exception e, String errorCode) {
        this();
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
