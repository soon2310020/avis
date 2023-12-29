package com.stg.service3rd.common.exception;

import com.stg.errors.ApplicationException;
import com.stg.errors.dto.ErrorDto;

public class Api3rdException extends ApplicationException {
    public Api3rdException(String message) {
        super(message);
    }

    public Api3rdException(String message, Throwable cause) {
        super(message, cause);
    }

    public Api3rdException(String message, ErrorDto errorDto) {
        super(message, errorDto);
    }

}
