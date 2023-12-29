package com.stg.errors;

public class CrmDataNotFoundException extends ApplicationException {
    public CrmDataNotFoundException(String message) {
        super(message);
    }

    public CrmDataNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
