package com.stg.errors.handle;

import com.stg.errors.ApplicationException;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps an exception to an {@link ExceptionHandlingConfiguration}.
 */
public abstract class ExceptionConfigurationMapper {

    private static final ExceptionHandlingConfiguration DEFAULT_CONFIGURATION =
            ExceptionHandlingConfiguration.Builder
                    .create()
                    .withHttpStatus(HttpStatus.BAD_REQUEST) //default
                    .withLogLevel(LogLevel.ERROR)
                    .withLogWithStacktrace(true)
                    .build();

    private final Map<Class<? extends ApplicationException>, ExceptionHandlingConfiguration> errorHttpStatusMap
            = new HashMap<>();

    protected ExceptionConfigurationMapper() {
        addMappings();
    }

    protected abstract void addMappings();

    /**
     * Returns the {@link ExceptionHandlingConfiguration} to which the specified exception class is mapped,
     * or the default exception handling configuration if this map contains no mapping for the exception class.
     */
    public ExceptionHandlingConfiguration get(Class<? extends ApplicationException> exceptionClazz) {
        return errorHttpStatusMap.getOrDefault(exceptionClazz, DEFAULT_CONFIGURATION);
    }

    protected void addMapping(Class<? extends ApplicationException> exceptionClazz,
                              ExceptionHandlingConfiguration configuration) {
        errorHttpStatusMap.put(exceptionClazz, configuration);
    }

}
