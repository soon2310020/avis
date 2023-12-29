package com.stg.errors;

public class InsurancePaymentNotFoundException extends ApplicationException {
    public InsurancePaymentNotFoundException(String message) {
        super(message);
    }

    public InsurancePaymentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
