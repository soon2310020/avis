package com.stg.errors;

public class PaginationException extends ApplicationException {
    public PaginationException(String message) {
        super(message);
    }

    public PaginationException(String message, Throwable cause) {
        super(message, cause);
    }
}
