package com.stg.errors;

public class SendEmailException extends ApplicationException {
    public SendEmailException(String message) {
        super(message);
    }

    public SendEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
