package com.stg.service3rd.mbalv2.dto.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.stg.service3rd.common.dto.soap.EResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class InquiryPaymentOuput {
    
    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("E_POLICY")
    private List<InquiryPaymentPolicy> policies;
    
    @JsonProperty("EResponse")
    private EResponse message;
    
}
