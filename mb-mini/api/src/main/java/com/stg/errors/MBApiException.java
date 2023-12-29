package com.stg.errors;

public class MBApiException extends ApplicationException {
    public MBApiException(String message) {
        super(message);
    }

    public MBApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
