package com.stg.errors;

public class EmailAndPasswordCredentialResetNotFoundException extends ApplicationException {

    public EmailAndPasswordCredentialResetNotFoundException(String message) {
        super(message);
    }

    public EmailAndPasswordCredentialResetNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
