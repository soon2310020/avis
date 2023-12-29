package com.stg.errors.mbal;

import com.stg.errors.ApplicationException;

public class MbalValidationException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public MbalValidationException(String message) {
        super(message);
    }
}
