package com.stg.errors;

public class CredentialHasExpiredException extends ApplicationException {

    public CredentialHasExpiredException(String message) {
        super(message);
    }

    public CredentialHasExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
