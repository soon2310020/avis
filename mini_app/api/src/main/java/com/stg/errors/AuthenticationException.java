package com.stg.errors;

import com.stg.errors.dto.ErrorDto;

public class AuthenticationException extends ApplicationException {
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthenticationException(String message, ErrorDto errorDto) {
        super(message, errorDto);
    }
}
