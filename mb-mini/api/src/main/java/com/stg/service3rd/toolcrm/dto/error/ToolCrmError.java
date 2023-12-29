package com.stg.service3rd.toolcrm.dto.error;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.stg.service3rd.common.dto.error.IErrorObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ToolCrmError implements IErrorObject {
    private String requestUid;
    private Long timestamp;

    private int httpStatus;
    private String message;
    private String errorCode;

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
