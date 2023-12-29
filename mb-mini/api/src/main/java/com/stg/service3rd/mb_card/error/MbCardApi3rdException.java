package com.stg.service3rd.mb_card.error;

import com.stg.errors.dto.ErrorDto;
import com.stg.service3rd.common.exception.Api3rdException;

public class MbCardApi3rdException extends Api3rdException {
    public MbCardApi3rdException(String message) {
        super(message);
    }

    public MbCardApi3rdException(String message, Throwable cause) {
        super(message, cause);
    }

    public MbCardApi3rdException(String message, ErrorDto errorDto) {
        super(message, errorDto);
    }
}
