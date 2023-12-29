package com.stg.service3rd.mic.exception;

import com.stg.errors.dto.ErrorDto;
import com.stg.service3rd.common.exception.Api3rdException;

public class MicApi3rdException extends Api3rdException {
    public MicApi3rdException(String message) {
        super(message);
    }

    public MicApi3rdException(String message, Throwable cause) {
        super(message, cause);
    }

    public MicApi3rdException(String message, ErrorDto errorDto) {
        super(message, errorDto);
    }
}
