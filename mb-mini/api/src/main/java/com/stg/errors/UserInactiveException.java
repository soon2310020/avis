package com.stg.errors;

public class UserInactiveException extends ApplicationException {
    public UserInactiveException(String message) {
        super(message);
    }

    public UserInactiveException(String message, Throwable cause) {
        super(message, cause);
    }
}
