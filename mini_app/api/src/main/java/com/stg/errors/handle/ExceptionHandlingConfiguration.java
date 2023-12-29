package com.stg.errors.handle;

import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

public class ExceptionHandlingConfiguration {

    public static class Builder {

        private HttpStatus httpStatus;
        private LogLevel logLevel;
        private boolean logStackTrace;

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder withHttpStatus(HttpStatus httpStatus) {
            this.httpStatus = httpStatus;
            return this;
        }

        public Builder withLogLevel(LogLevel logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        public Builder withLogWithStacktrace(boolean logStackTrace) {
            this.logStackTrace = logStackTrace;
            return this;
        }

        public ExceptionHandlingConfiguration build() {
            return new ExceptionHandlingConfiguration(httpStatus, logLevel, logStackTrace);
        }
    }


    private final HttpStatus httpStatus;
    private final LogLevel logLevel;
    private final boolean logStackTrace;

    private ExceptionHandlingConfiguration(HttpStatus httpStatus, LogLevel logLevel, boolean logStackTrace) {
        this.httpStatus = httpStatus;
        this.logLevel = logLevel;
        this.logStackTrace = logStackTrace;
    }

    HttpStatus getHttpStatus() {
        return httpStatus;
    }

    LogLevel getLogLevel() {
        return logLevel;
    }

    boolean isLogStackTrace() {
        return logStackTrace;
    }
}


