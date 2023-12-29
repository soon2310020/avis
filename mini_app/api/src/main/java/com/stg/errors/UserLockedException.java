package com.stg.errors;

public class UserLockedException extends ApplicationException {
    public UserLockedException(String message) {
        super(message);
    }

    public UserLockedException(String message, Throwable cause) {
        super(message, cause);
    }
}
