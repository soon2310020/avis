package com.stg.service3rd.mbalv2.dto.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stg.service3rd.common.dto.soap.EResponse;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CalcPaymentEAppOutput {
    @JsonProperty("POLICYNR_TT")
    private String policyNrTT;

    @JsonProperty("PAGNO_ID")
    private String pagNoId;

    @JsonProperty("STATUS")
    private String status;

    @JsonProperty("PM_ID")
    private String pmId;

    @JsonProperty("PRODUCT_NAME")
    private String productName;

    @JsonProperty("HOLDER_BP")
    private String holderBp;

    @JsonProperty("HOLDER_NAME")
    private String holderName;

    @JsonProperty("HOLDER_DOB")
    private String holderDob;

    @JsonProperty("INSURED_BP")
    private String insuredBp;

    @JsonProperty("INSURED_NAME")
    private String insuredName;

    @JsonProperty("INSURED_DOB")
    private String insuredDob;

    @JsonProperty("ALLOW_CREDIT_CARD")
    private String allowCreditCard;

    @JsonProperty("PAYMENT_FREQUENCY")
    private String paymentFrequency;

    @JsonProperty("PREMAFTERTAX_AM")
    private String premafterTaxAm;

    @JsonProperty("TOP_UP")
    private String topUp;

    @JsonProperty("TOTALPAY_AM")
    private String totalPayAM;

    @JsonProperty("INQUIRY_ID")
    private String inquiryId;

    @JsonProperty("SYSTEM_DATE")
    private String systemDate;

    @JsonProperty("SYSTEM_TIME")
    private String systemTime;


    /*Error message*/
    @JsonProperty("EResponse")
    private EResponse message;
}
