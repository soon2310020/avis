package com.stg.errors;

public class MicPackageNotFoundException extends ApplicationException {
    public MicPackageNotFoundException(String message) {
        super(message);
    }

    public MicPackageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
