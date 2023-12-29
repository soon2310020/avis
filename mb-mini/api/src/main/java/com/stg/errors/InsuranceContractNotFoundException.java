package com.stg.errors;

public class InsuranceContractNotFoundException extends ApplicationException {
    public InsuranceContractNotFoundException(String message) {
        super(message);
    }

    public InsuranceContractNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
