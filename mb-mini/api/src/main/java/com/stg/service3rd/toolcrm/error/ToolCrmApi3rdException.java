package com.stg.service3rd.toolcrm.error;

import com.stg.errors.dto.ErrorDto;
import com.stg.service3rd.common.exception.Api3rdException;

public class ToolCrmApi3rdException extends Api3rdException {
    public ToolCrmApi3rdException(String message) {
        super(message);
    }

    public ToolCrmApi3rdException(String message, Throwable cause) {
        super(message, cause);
    }

    public ToolCrmApi3rdException(String message, ErrorDto errorDto) {
        super(message, errorDto);
    }
}
