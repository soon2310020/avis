package com.stg.errors;

public class MBApiAuthenticationException extends ApplicationException {
    public MBApiAuthenticationException(String message) {
        super(message);
    }

    public MBApiAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
