package com.stg.errors.quote;

import com.stg.errors.ApplicationException;
import com.stg.errors.dto.ErrorDto;

public class CustomerPermissionException extends ApplicationException {
    public CustomerPermissionException(String message) {
        super(message);
    }

    public CustomerPermissionException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomerPermissionException(String message, ErrorDto errorDto) {
        super(message, errorDto);
    }

}
