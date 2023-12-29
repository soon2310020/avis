package com.stg.errors.handle;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.stg.errors.ApplicationException;
import com.stg.errors.dto.ErrorDetailDto;
import com.stg.errors.dto.ErrorDto;
import com.stg.errors.dto.ErrorDtoWithDetails;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@ControllerAdvice
public class ExceptionHandlerControllerAdvice {
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandlerControllerAdvice.class);
    private static final String DOUBLE_BRACES = "{}: {}";

    private final ExceptionConfigurationMapper errorHttpStatusMapping;
    private final Environment environment;
    
    /**
     * The most generic handler. Handles any exception that was not handled by other handlers.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> process(Exception ex) {
        Throwable cause = findCauseUsingPlainJava(ex);

        if (cause instanceof ConstraintViolationException) {
            return process((ConstraintViolationException) cause);
        }

        logException(ex, LogLevel.ERROR, true);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, ex));
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDtoWithDetails> process(ConstraintViolationException ex) {
        logException(ex, LogLevel.INFO, false);

        return ResponseEntity.badRequest().body(createErrorDtoWithDetails(HttpStatus.BAD_REQUEST,
                extractMessage(ex.getConstraintViolations()),
                ex.getConstraintViolations()));
    }

    /**
     * Handles all custom exceptions. More specific handlers can be declared in any specific application.
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorDto> process(ApplicationException ex) {
        ErrorDto errorDto = ex.getErrorDto();
        if (errorDto != null) {
            return ResponseEntity.status(errorDto.getHttpStatus()).body(errorDto);
        }

        ExceptionHandlingConfiguration config = errorHttpStatusMapping.get(ex.getClass());
        logException(ex, config.getLogLevel(), config.isLogStackTrace());

        return ResponseEntity.status(config.getHttpStatus())
                .body(new ErrorDto(config.getHttpStatus(), ex, ex.getErrorCode()));
    }


    /**
     * Handles dto validation errors enforced with javax.validation.constraints or org.hibernate.validator.constraints
     * annotations on the fields of a dto.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDtoWithDetails process(MethodArgumentNotValidException ex) {
        logException(ex, LogLevel.INFO, false);

        return createErrorDtoWithDetails(HttpStatus.BAD_REQUEST,
                extractMessage(ex.getBindingResult()),
                ex.getBindingResult());
    }

    /**
     * Handles method argument type mismatches, caused by client sending badly typed arguments to the service.
     * <p>
     * E.g. sending String instead of an expected Long, causing this MethodArgumentTypeMismatchException to be thrown,
     * wrapping a NumberFormatException or such.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDto process(MethodArgumentTypeMismatchException ex) {
        logException(ex, LogLevel.INFO, false);

        String message = "MethodArgumentTypeMismatch on name=" + ex.getName() +
                " and type=" + ex.getParameter().getParameterType();

        return new ErrorDto(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Handles validation errors enforced with javax.validation.constraints or org.hibernate.validator.constraints
     * annotations on a POJO bound to query parameters of a web service request with <code>@ModelAttribute</code>.
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDtoWithDetails process(BindException ex) {
        logException(ex, LogLevel.INFO, false);

        return createErrorDtoWithDetails(HttpStatus.BAD_REQUEST,
                extractMessage(ex.getBindingResult()),
                ex.getBindingResult());
    }

    /**
     * Handles the case when a mandatory query parameter is missing on a web service request.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDto process(MissingServletRequestParameterException ex) {
        logException(ex, LogLevel.INFO, false);

        return new ErrorDto(HttpStatus.BAD_REQUEST, ex);
    }

    /**
     * Handles the case when a mandatory query parameter is missing on a web service request.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDto process(HttpMessageNotReadableException ex) {
    	logException(ex, LogLevel.INFO, false);
    	
    	if (ex.getCause() instanceof JsonMappingException) {
    		if (Arrays.stream(environment.getActiveProfiles()).anyMatch("dev"::equals)) {
    			JsonMappingException cause = (JsonMappingException) ex.getCause();
    			return new ErrorDto(HttpStatus.BAD_REQUEST, cause);
    		}
    	}
    	
    	return new ErrorDto(HttpStatus.BAD_REQUEST, (String) null);
    }
    
    @ExceptionHandler(HttpMessageConversionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDto process(HttpMessageConversionException ex) {
    	logException(ex, LogLevel.INFO, false);
    	
    	if (Arrays.stream(environment.getActiveProfiles()).anyMatch("dev"::equals)) {
			return new ErrorDto(HttpStatus.BAD_REQUEST, ex);
		}

        return new ErrorDto(HttpStatus.BAD_REQUEST, (String) null);
    }

    /**
     * Handles IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDto process(IllegalArgumentException ex) {
        logException(ex, LogLevel.INFO, false);

        return new ErrorDto(HttpStatus.BAD_REQUEST, ex);
    }

    private Throwable findCauseUsingPlainJava(Throwable throwable) {
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }
    
    private String extractMessage(BindingResult bindingResult) {
        return bindingResult.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(". "));
    }
    
    private String extractMessage(Set<ConstraintViolation<?>> constraintViolations) {
        return constraintViolations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(". "));
    }

    private ErrorDtoWithDetails createErrorDtoWithDetails(HttpStatus httpStatus, String message, BindingResult bindingResult) {
        return new ErrorDtoWithDetails(httpStatus, message, createErrorDetailDtos(bindingResult));
    }

    private ErrorDtoWithDetails createErrorDtoWithDetails(HttpStatus httpStatus, String message, Set<ConstraintViolation<?>> constraintViolations) {
        return new ErrorDtoWithDetails(httpStatus, message, createErrorDetailDtos(constraintViolations));
    }
    
    private static List<ErrorDetailDto> createErrorDetailDtos(BindingResult bindingResult) {
        Stream<ErrorDetailDto> globalErrorsStream = bindingResult.getGlobalErrors().stream()
                .map(globalError -> new ErrorDetailDto(globalError.getObjectName(), globalError.getDefaultMessage()));

        Stream<ErrorDetailDto> fieldErrorsStream = bindingResult.getFieldErrors().stream()
                .map(fieldError -> new ErrorDetailDto(convertToSnakeCase(fieldError.getField()), fieldError.getDefaultMessage()));

        return Stream.concat(globalErrorsStream, fieldErrorsStream).collect(Collectors.toList());
    }
    
    private static List<ErrorDetailDto> createErrorDetailDtos(Set<ConstraintViolation<?>> constraintViolations) {

        Stream<ErrorDetailDto> errorsStream = constraintViolations.stream()
                .map(error -> new ErrorDetailDto(
                        error.getRootBeanClass().getSimpleName() + "." + error.getPropertyPath().toString(),
                        error.getMessage()));

        return errorsStream.collect(Collectors.toList());
    }
    
    private static String convertToSnakeCase(String input) {
        final String regex = "([a-z])([A-Z])";
        final String replacement = "$1_$2";
        return input
          .replaceAll(regex, replacement)
          .toLowerCase();
    }

    protected static void logException(Exception e, LogLevel level, boolean shouldLogStackTrace) {
        if (level == LogLevel.ERROR) {
            if (shouldLogStackTrace) {
                LOG.error(DOUBLE_BRACES, e.getClass().getSimpleName(), e.getMessage(), e);
            } else {
                LOG.error(DOUBLE_BRACES, e.getClass().getSimpleName(), e.getMessage());
            }
        } else if (level == LogLevel.WARN) {
            if (shouldLogStackTrace) {
                LOG.warn(DOUBLE_BRACES, e.getClass().getSimpleName(), e.getMessage(), e);
            } else {
                LOG.warn(DOUBLE_BRACES, e.getClass().getSimpleName(), e.getMessage());
            }
        } else if (level == LogLevel.INFO) {
            if (shouldLogStackTrace) {
                LOG.info(DOUBLE_BRACES, e.getClass().getSimpleName(), e.getMessage(), e);
            } else {
                LOG.info(DOUBLE_BRACES, e.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

}
