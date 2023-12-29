package com.stg.errors.quote;

import com.stg.errors.ApplicationException;
import com.stg.errors.dto.ErrorDto;

public class CreateQuoteException extends ApplicationException {
    public CreateQuoteException(String message) {
        super(message);
    }

    public CreateQuoteException(String message, Throwable cause) {
        super(message, cause);
    }

    public CreateQuoteException(String message, ErrorDto errorDto) {
        super(message, errorDto);
    }

}
