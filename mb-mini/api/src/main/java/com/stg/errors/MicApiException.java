package com.stg.errors;

public class MicApiException extends ApplicationException {
    public MicApiException(String message) {
        super(message);
    }

    public MicApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
