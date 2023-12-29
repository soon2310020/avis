package com.stg.errors;


import com.stg.errors.dto.ErrorDto;
import lombok.Getter;
import lombok.Setter;

/**
 * Generic exception that is intended to be the super class of all custom exceptions.
 */
@Getter
@Setter
public class ApplicationException extends RuntimeException {

    private transient ErrorDto errorDto;

    public ApplicationException(String message) {
        super(message);
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }


    public ApplicationException(String message, ErrorDto errorDto) {
        super(message);
        this.errorDto = errorDto;
    }

    public String getErrorCode() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return "Application Exception{} " + super.toString();
    }
}
