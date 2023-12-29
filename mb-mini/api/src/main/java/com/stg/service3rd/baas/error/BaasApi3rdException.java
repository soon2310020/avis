package com.stg.service3rd.baas.error;

import com.stg.errors.dto.ErrorDto;
import com.stg.service3rd.common.exception.Api3rdException;

public class BaasApi3rdException extends Api3rdException {
    public BaasApi3rdException(String message) {
        super(message);
    }

    public BaasApi3rdException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaasApi3rdException(String message, ErrorDto errorDto) {
        super(message, errorDto);
    }
}
