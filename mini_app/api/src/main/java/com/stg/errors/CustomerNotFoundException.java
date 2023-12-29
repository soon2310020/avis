package com.stg.errors;


public class CustomerNotFoundException extends ApplicationException {
    public CustomerNotFoundException(String message) {
        super(message);
    }
    public CustomerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
