package com.stg.errors;

public class AuthenticateFailException extends ApplicationException {

    public AuthenticateFailException(String message) {
        super(message);
    }

    public AuthenticateFailException(String message, Throwable cause) {
        super(message, cause);
    }
}
