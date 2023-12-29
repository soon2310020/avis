package com.stg.service3rd.mbalv2.dto.req;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.stg.service3rd.common.dto.soap.SoapRequestBody;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalcPaymentPolicyReq implements SoapRequestBody {
    @JacksonXmlProperty(localName = "urn:MT_CALC_PAYMENT_INPUT")
    private CalcPaymentPolicyInput input = new CalcPaymentPolicyInput();
}
