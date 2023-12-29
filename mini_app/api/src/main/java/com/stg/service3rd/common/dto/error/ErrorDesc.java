package com.stg.service3rd.common.dto.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static com.stg.service3rd.common.utils.LogUtil.errorDescToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDesc implements IErrorObject {
    private int httpStatus; /*default*/

    private String errorCode;
    private List<String> errorDesc;

    /***/
    @Override
    public String getErrorMessage() {
        return errorDescToString(errorDesc);
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        if (errorDesc == null) errorDesc = new ArrayList<>();
        errorDesc.add(errorMessage);
    }
}
