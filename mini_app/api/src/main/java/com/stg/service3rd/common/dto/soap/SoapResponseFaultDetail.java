package com.stg.service3rd.common.dto.soap;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SoapResponseFaultDetail {
    @JsonProperty("SystemError")
    private SoapResponseFaultDetailError error;

    public String getMessage() {
        return error.getText();
    }
    
    @Setter
    @Getter
    public static class SoapResponseFaultDetailError {
        @JsonProperty("context")
        private String context;
        @JsonProperty("code")
        private String code;
        @JsonProperty("text")
        private String text;
    }
}
