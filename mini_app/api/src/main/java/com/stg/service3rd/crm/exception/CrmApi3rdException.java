package com.stg.service3rd.crm.exception;

import com.stg.errors.dto.ErrorDto;
import com.stg.service3rd.common.exception.Api3rdException;

public class CrmApi3rdException extends Api3rdException {
    public CrmApi3rdException(String message) {
        super(message);
    }

    public CrmApi3rdException(String message, Throwable cause) {
        super(message, cause);
    }

    public CrmApi3rdException(String message, ErrorDto errorDto) {
        super(message, errorDto);
    }
}
