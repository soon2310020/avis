package com.stg.errors;

public class IllustrationTableNotFoundException extends ApplicationException {
    public IllustrationTableNotFoundException(String message) {
        super(message);
    }

    public IllustrationTableNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
