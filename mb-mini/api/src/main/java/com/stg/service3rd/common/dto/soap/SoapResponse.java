package com.stg.service3rd.common.dto.soap;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SoapResponse<B extends SoapResponseBody> {
    @JsonProperty("Header")
    private SoapHeader header;
    
    @JsonProperty("Body")
    private B body;
}
