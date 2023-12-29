package com.stg.service3rd.mbalv2.dto.req;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CalcPaymentPolicyInput {

    @JacksonXmlProperty(localName = "I_ID_NUMBER")
    private String number;
    
}
