package com.stg.errors;

public class CredentialNotMatchingException extends ApplicationException {

    public CredentialNotMatchingException(String message) {
        super(message);
    }

    public CredentialNotMatchingException(String message, Throwable cause) {
        super(message, cause);
    }
}
