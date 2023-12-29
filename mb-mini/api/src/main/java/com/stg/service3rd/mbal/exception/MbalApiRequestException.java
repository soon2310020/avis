package com.stg.service3rd.mbal.exception;

import com.stg.service3rd.common.exception.Api3rdException;

public class MbalApiRequestException extends Api3rdException {
    public MbalApiRequestException(String message) {
        super(message);
    }

    public MbalApiRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
