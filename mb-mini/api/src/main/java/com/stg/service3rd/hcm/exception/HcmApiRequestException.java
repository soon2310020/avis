package com.stg.service3rd.hcm.exception;

import com.stg.errors.ApplicationException;
import com.stg.errors.dto.ErrorDto;

public class HcmApiRequestException extends ApplicationException {
    public HcmApiRequestException(String message) {
        super(message);
    }

    public HcmApiRequestException(String message, Throwable cause) {
        super(message, cause);
    }
    public HcmApiRequestException(String message, ErrorDto errorDto) {
        super(message, errorDto);
    }
}
