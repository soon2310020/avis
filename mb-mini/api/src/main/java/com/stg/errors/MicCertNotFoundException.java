package com.stg.errors;

public class MicCertNotFoundException extends ApplicationException {
    public MicCertNotFoundException(String message) {
        super(message);
    }

    public MicCertNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
