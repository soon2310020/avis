package com.stg.service3rd.mic_sandbox.dto.error;

import com.stg.service3rd.common.dto.error.IErrorObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MicSandboxError implements IErrorObject {
    private int httpStatus;
    private String message;
    private String code;

    //private String data; // always null

    /***/
    @Override
    public String getErrorCode() {
        return this.code;
    }

    @Override
    public void setErrorCode(String errorCode) {
        this.code = errorCode;
    }

    /***/
    @Override
    public String getErrorMessage() {
        return this.message;
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        this.message = errorMessage;
    }
}
