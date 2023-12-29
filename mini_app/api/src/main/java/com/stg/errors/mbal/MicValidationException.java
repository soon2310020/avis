package com.stg.errors.mbal;

import com.stg.errors.ApplicationException;

public class MicValidationException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public MicValidationException(String message) {
        super(message);
    }
}
