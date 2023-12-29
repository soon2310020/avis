package com.stg.service3rd.common.dto.soap;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SoapResponseFault {
    @JsonProperty("faultcode")
    private String code;
    @JsonProperty("faultstring")
    private String text;
    @JsonProperty("detail")
    private SoapResponseFaultDetail detail;
}
