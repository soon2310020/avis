package com.stg.errors;

import com.stg.errors.dto.ErrorDto;

public class MbalApiException extends ApplicationException {
    public MbalApiException(String message) {
        super(message);
    }

    public MbalApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public MbalApiException(String message, ErrorDto errorDto) {
        super(message, errorDto);
    }
}
