package com.stg.errors;

public class InstallmentApiException extends ApplicationException {
    public InstallmentApiException(String message) {
        super(message);
    }

    public InstallmentApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
