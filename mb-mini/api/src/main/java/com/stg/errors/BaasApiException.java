package com.stg.errors;

public class BaasApiException extends ApplicationException {
    public BaasApiException(String message) {
        super(message);
    }

    public BaasApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
