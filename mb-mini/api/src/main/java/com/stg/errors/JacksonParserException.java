package com.stg.errors;

import com.stg.errors.dto.ErrorDto;

public class JacksonParserException extends ApplicationException {
    public JacksonParserException(String message) {
        super(message);
    }

    public JacksonParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public JacksonParserException(String message, ErrorDto errorDto) {
        super(message, errorDto);
    }
}
