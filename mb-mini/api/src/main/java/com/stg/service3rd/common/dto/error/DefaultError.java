package com.stg.service3rd.common.dto.error;

import com.stg.errors.dto.ErrorDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DefaultError implements IErrorObject {
    private int httpStatus; /*default*/

    private String errorCode;
    private String errorMessage;

    public DefaultError(ErrorDto errorDto) {
        this.httpStatus = errorDto.getHttpStatus();
        this.errorCode = errorDto.getErrorCode();
        this.errorMessage = errorDto.getMessage();
    }
}
