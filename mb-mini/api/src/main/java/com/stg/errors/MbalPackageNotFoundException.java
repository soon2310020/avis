package com.stg.errors;

public class MbalPackageNotFoundException extends ApplicationException {
    public MbalPackageNotFoundException(String message) {
        super(message);
    }

    public MbalPackageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
