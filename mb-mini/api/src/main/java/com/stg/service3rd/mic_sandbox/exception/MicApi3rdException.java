package com.stg.service3rd.mic_sandbox.exception;

import com.stg.errors.ApplicationException;
import com.stg.errors.dto.ErrorDto;

public class MicApi3rdException extends ApplicationException {
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
