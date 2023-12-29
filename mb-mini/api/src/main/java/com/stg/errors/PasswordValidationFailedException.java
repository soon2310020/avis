package com.stg.errors;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordValidationFailedException extends ApplicationException {

    public PasswordValidationFailedException(String message) {
        super(message);
    }

    public PasswordValidationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
