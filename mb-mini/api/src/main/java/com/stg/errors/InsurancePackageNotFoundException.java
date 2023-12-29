package com.stg.errors;

public class InsurancePackageNotFoundException extends ApplicationException {
    public InsurancePackageNotFoundException(String message) {
        super(message);
    }

    public InsurancePackageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
