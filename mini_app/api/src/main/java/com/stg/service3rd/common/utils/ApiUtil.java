package com.stg.service3rd.common.utils;

import com.stg.common.Jackson;
import com.stg.errors.ApplicationException;
import com.stg.service3rd.common.dto.error.DefaultError;
import com.stg.service3rd.common.dto.error.ErrorAutoDetect;
import com.stg.service3rd.common.dto.error.IErrorObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiUtil {
    private static final String UN_MATCH_ERROR_CODE = "UNM"; /*RestClientResponseException, but PARSE error!*/
    private static final String RESOURCE_ACCESS_ERROR_CODE = "RAE500";
    private static final String APP_UNKNOWN_ERROR_CODE = "AUK500";

    /**/
    public static final String UNKNOWN_ERROR_CODE = "UNK500";
    public static final String SERVER_ERROR_MESSAGE = "Rất tiếc!!! Hệ thống đang nâng cấp, vui lòng thử lại sau";


    /***/
    public static String map(@NonNull String uriTemp, @NonNull Map<String, ?> pathVariables) {
        for (Map.Entry<String, ?> entry : pathVariables.entrySet()) {
            uriTemp = uriTemp.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return uriTemp;
    }


    /***/
    public static String getFirstHeader(HttpHeaders headers, String headerKey, String defaultValue) {
        if (headers == null) return defaultValue;
        String value = headers.getFirst(headerKey);
        return value == null ? defaultValue : value;
    }

    public static String getFirstHeader(HttpHeaders headers, String headerKey) {
        return getFirstHeader(headers, headerKey, null);
    }


    /**
     * [EXCEPTION]
     * > RestClientException:
     * >> RestClientResponseException: >>>HttpClientErrorException,...
     * >> ResourceAccessException
     * <p>
     * [ERROR-EXCEPTION-DETAIL]
     * 1.HttpClientErrorException – in the case of HTTP status 4xx
     * 2.HttpServerErrorException – in the case of HTTP status 5xx
     * 3.UnknownHttpStatusCodeException – in the case of an unknown HTTP status
     */
    public static <E extends IErrorObject> IErrorObject parseErrorMessage(Exception respException, Class<E> errorType) {
        IErrorObject errorDto;

        //APPLICATION ???throws...
        if (respException instanceof ApplicationException) {
            ApplicationException appException = (ApplicationException) respException;
            if (appException.getErrorDto() != null) {
                errorDto = new DefaultError(appException.getErrorDto());
            } else {
                errorDto = new DefaultError(HttpStatus.BAD_REQUEST.value(), APP_UNKNOWN_ERROR_CODE, appException.getMessage());
            }
        }
        //FROM: 3rd party
        else if (respException instanceof RestClientResponseException) {
            RestClientResponseException clientRespException = (RestClientResponseException) respException;
            errorDto = parseErrorString(clientRespException.getResponseBodyAsString(), errorType); /*convert*/
            if (errorDto != null) {
                errorDto.setHttpStatus(clientRespException.getRawStatusCode());
                if (errorDto.getErrorCode() == null) {
                    errorDto.setErrorCode(UN_MATCH_ERROR_CODE + clientRespException.getRawStatusCode());
                }
                if (errorDto.getErrorMessage() == null) errorDto.setErrorMessage(SERVER_ERROR_MESSAGE);
            } else {
                errorDto = new DefaultError(clientRespException.getRawStatusCode(), UN_MATCH_ERROR_CODE + clientRespException.getRawStatusCode(), SERVER_ERROR_MESSAGE);
            }
        }
        //FROM: RestTemplate ? HttpStatus.REQUEST_TIMEOUT,...
        else if (respException instanceof ResourceAccessException) {
            errorDto = new DefaultError(HttpStatus.REQUEST_TIMEOUT.value(), RESOURCE_ACCESS_ERROR_CODE, SERVER_ERROR_MESSAGE);
        }
        //UNKNOWN
        else {
            errorDto = new DefaultError(HttpStatus.INTERNAL_SERVER_ERROR.value(), UNKNOWN_ERROR_CODE, SERVER_ERROR_MESSAGE);
        }

        return errorDto;
    }

    public static IErrorObject parseErrorMessage(Exception respException) {
        return parseErrorMessage(respException, ErrorAutoDetect.class);
    }

    /**
     * @return null <=> empty or un-match
     */
    private static <E extends IErrorObject> IErrorObject parseErrorString(@Nullable String errorBodyString, @NonNull Class<E> errorType) {
        if (!StringUtils.hasText(errorBodyString)) {
            return null;
        }

        try {
            return Jackson.get().fromJson(errorBodyString, errorType);
        } catch (Exception ex) {
            return null;
        }
    }


    /***/
    public static String convertMessageToString(@lombok.NonNull Object objError) {
        if (objError instanceof List<?>) {
            try {
                return org.apache.commons.lang.StringUtils.join((List<?>) objError, ", ");
            } catch (Exception ex) {
                log.error("error while convertMessageToString!", ex);
                return null;
            }
        }
        return objError.toString();
    }
}
