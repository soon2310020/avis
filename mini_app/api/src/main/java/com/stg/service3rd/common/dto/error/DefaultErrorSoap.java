package com.stg.service3rd.common.dto.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DefaultErrorSoap implements IErrorObject {
    private int httpStatus; /*default*/

    @JsonProperty("ResponseCode")
    private String errorCode;
    @JsonProperty("ResponseStrEn")
    private String messageEn;
    @JsonProperty("ResponseStrVi")
    private String messageVi;

    /***/
    @Override
    public String getErrorMessage() {
        return this.messageVi != null ? this.messageVi : this.messageEn;
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        this.messageVi = errorMessage;
    }
}
