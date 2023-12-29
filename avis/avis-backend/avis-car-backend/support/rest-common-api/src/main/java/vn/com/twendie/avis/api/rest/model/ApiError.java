package vn.com.twendie.avis.api.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import vn.com.twendie.avis.api.rest.exception.BaseApiException;
import vn.com.twendie.avis.locale.config.Translator;

import javax.validation.ConstraintViolationException;
import java.util.Collections;
import java.util.List;

@Data
@Builder
public class ApiError {

    @JsonProperty("code")
    private int code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("debug_message")
    private String debugMessage;

    @JsonProperty("extra_data")
    private Object extraData;

    public static ApiError buildApiError(Throwable throwable, HttpStatus httpStatus) {
        if (throwable instanceof ConstraintViolationException) {
            return buildApiError(((ConstraintViolationException) throwable));
        }
        if (throwable instanceof MethodArgumentNotValidException) {
            return buildApiError((MethodArgumentNotValidException) throwable);
        }
        if (throwable instanceof BaseApiException) {
            return buildApiError((BaseApiException) throwable);
        } else {
            return ApiError.builder()
                    .code(httpStatus.value())
                    .message(Translator.toLocale("error.default"))
                    .debugMessage(String.valueOf(throwable))
                    .extraData(Collections.EMPTY_MAP)
                    .build();
        }
    }

    private static ApiError buildApiError(ConstraintViolationException e) {
        String messageCode = e.getConstraintViolations().iterator().next().getMessage();
        String defaultMessage = Translator.toLocale("error.invalid_input");
        return ApiError.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(Translator.toLocale(messageCode, defaultMessage))
                .debugMessage(e.toString())
                .extraData(Collections.EMPTY_MAP)
                .build();
    }

    private static ApiError buildApiError(MethodArgumentNotValidException e) {
        List<ObjectError> errors = e.getBindingResult().getAllErrors();
        String messageCode = errors.isEmpty() ? "" : errors.get(0).getDefaultMessage();
        String defaultMessage = Translator.toLocale("error.invalid_input");
        return ApiError.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(Translator.toLocale(messageCode, defaultMessage))
                .debugMessage(e.toString())
                .extraData(Collections.EMPTY_MAP)
                .build();
    }

    private static ApiError buildApiError(BaseApiException e) {
        return ApiError.builder()
                .code(e.code())
                .message(StringUtils.defaultIfBlank(e.displayMessage(), Translator.toLocale("error.default")))
                .debugMessage(e.toString())
                .extraData(e.extraData())
                .build();
    }

}
