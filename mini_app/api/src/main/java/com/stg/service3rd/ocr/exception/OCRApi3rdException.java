package com.stg.service3rd.ocr.exception;

import com.stg.errors.ApplicationException;
import com.stg.errors.dto.ErrorDto;

public class OCRApi3rdException extends ApplicationException {
    public OCRApi3rdException(String message) {
        super(message);
    }

    public OCRApi3rdException(String message, Throwable cause) {
        super(message, cause);
    }

    public OCRApi3rdException(String message, ErrorDto errorDto) {
        super(message, errorDto);
    }
}
