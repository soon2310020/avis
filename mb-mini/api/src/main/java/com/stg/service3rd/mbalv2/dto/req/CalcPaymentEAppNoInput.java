package com.stg.service3rd.mbalv2.dto.req;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CalcPaymentEAppNoInput {

    @JacksonXmlProperty(localName = "Inquiry_Type")
    private String inquiryType;

    @JacksonXmlProperty(localName = "Inquiry_Search")
    private String inquirySearch;

    @JacksonXmlProperty(localName = "ID_Number")
    private String idNumber;
}
