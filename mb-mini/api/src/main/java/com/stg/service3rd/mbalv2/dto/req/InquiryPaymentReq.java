package com.stg.service3rd.mbalv2.dto.req;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.stg.service3rd.common.dto.soap.SoapRequestBody;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InquiryPaymentReq implements SoapRequestBody {
    @JacksonXmlProperty(localName = "urn:MT_INQUIRY_PAYMENT_INPUT")
    private InquiryPaymentInput input = new InquiryPaymentInput();
}
