package com.stg.service3rd.mbal.exception;

import com.stg.errors.dto.ErrorDto;
import com.stg.service3rd.common.exception.Api3rdException;

public class MbalApi3rdException extends Api3rdException {
    public MbalApi3rdException(String message) {
        super(message);
    }

    public MbalApi3rdException(String message, Throwable cause) {
        super(message, cause);
    }

    public MbalApi3rdException(String message, ErrorDto errorDto) {
        super(message, errorDto);
    }
}
