package com.stg.errors;

import com.stg.errors.dto.ErrorDto;

public class EmployeeImportException extends ApplicationException{
    public EmployeeImportException(String message) {
        super(message);
    }

    public EmployeeImportException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmployeeImportException(String message, ErrorDto errorDto) {
        super(message, errorDto);
    }
}
