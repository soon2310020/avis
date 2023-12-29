package com.stg.errors;

public class UserHasNoPermissionException extends ApplicationException {

    public UserHasNoPermissionException(String message) {
        super(message);
    }

    public UserHasNoPermissionException(String message, Throwable cause) {
        super(message, cause);
    }
}
