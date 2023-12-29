package com.stg.errors;

public class CredentialNotFoundException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public CredentialNotFoundException(String message) {
        super(message);
    }
}
