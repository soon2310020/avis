package com.stg.errors;

public class MiniApiException extends ApplicationException {
    public MiniApiException(String message) {
        super(message);
    }

    public MiniApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
