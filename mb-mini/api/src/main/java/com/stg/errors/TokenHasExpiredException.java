package com.stg.errors;

public class TokenHasExpiredException extends ApplicationException {

    public TokenHasExpiredException(String message) {
        super(message);
    }

    public TokenHasExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
