package com.stg.errors;

public class EmailAndPasswordCredentialResetInvalidException extends ApplicationException {

    public EmailAndPasswordCredentialResetInvalidException(String message) {
        super(message);
    }

    public EmailAndPasswordCredentialResetInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}
