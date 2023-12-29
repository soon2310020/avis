package com.stg.service3rd.common.dto.error;

import com.stg.errors.dto.ErrorDto;

public interface IErrorObject {
    /**
     * NOTE: Always have field: "private int httpStatus;"
     */
    int getHttpStatus();
    void setHttpStatus(int httpStatus);


    /***/
    String getErrorCode();
    void setErrorCode(String errorCode);

    /***/
    String getErrorMessage();
    void setErrorMessage(String errorMessage);


    /***/
    default ErrorDto toErrorDto() {
        return new ErrorDto(this.getHttpStatus(), this.getErrorCode(), this.getErrorMessage());
    }
}
