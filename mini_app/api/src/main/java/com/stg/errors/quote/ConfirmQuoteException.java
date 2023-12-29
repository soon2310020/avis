package com.stg.errors.quote;

import com.stg.errors.ApplicationException;
import com.stg.errors.dto.ErrorDto;

public class ConfirmQuoteException extends ApplicationException {
    public ConfirmQuoteException(String message) {
        super(message);
    }

    public ConfirmQuoteException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfirmQuoteException(String message, ErrorDto errorDto) {
        super(message, errorDto);
    }
}
