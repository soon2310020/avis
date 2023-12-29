package com.stg.errors;

import com.stg.errors.dto.ErrorDto;

public class AutoDebitException extends ApplicationException {
    public AutoDebitException(String message) {
        super(message);
    }

    public AutoDebitException(String message, Throwable cause) {
        super(message, cause);
    }

    public AutoDebitException(String message, ErrorDto errorDto) {
        super(message, errorDto);
    }
}
