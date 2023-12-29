package com.stg.service3rd.common.dto.soap;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SoapResponseErrorBody extends SoapResponseBody {

    @JsonProperty("Fault")
    private SoapResponseFault fault;
    
    @Override
    public EResponse getMessage() {
        return null;
    }
}
