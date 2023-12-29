package com.stg.errors.quote;

import com.stg.errors.ApplicationException;
import com.stg.errors.dto.ErrorDto;

public class CreateProcessException extends ApplicationException {
    public CreateProcessException(String message) {
        super(message);
    }

    public CreateProcessException(String message, Throwable cause) {
        super(message, cause);
    }


    public CreateProcessException(String message, ErrorDto errorDto) {
        super(message, errorDto);
    }
}
