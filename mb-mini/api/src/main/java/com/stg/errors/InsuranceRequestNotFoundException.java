package com.stg.errors;

public class InsuranceRequestNotFoundException extends ApplicationException {
    public InsuranceRequestNotFoundException(String message) {
        super(message);
    }

    public InsuranceRequestNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
