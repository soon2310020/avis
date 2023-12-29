package com.stg.service3rd.mbalv2.dto.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CalcPaymentPolicy {
    @JsonProperty("POLICY_STT")
    private String policyStt;

    @JsonProperty("POLICY_NUM")
    private String policyNum;

    @JsonProperty("PRODUCT_ID")
    private String productId;

    @JsonProperty("PRODUCT_NAME")
    private String productName;

    @JsonProperty("INQUIRY_DATE")
    private String inquiryDate;

    @JsonProperty("POLICY_EFF_DATE")
    private String policyEffDate;

    @JsonProperty("PAYFREQ_TEXT")
    private String payfreqText;

    @JsonProperty("PERIODIC_PREM")
    private String periodicPrem;

    @JsonProperty("CURRENCY")
    private String currency;

    @JsonProperty("FEE_AMT")
    private String feeAmt;

    @JsonProperty("OVER_DUE_AMT")
    private String overDueAmt;

    @JsonProperty("SUSPENSE_AMT")
    private String suspenseAmt;

    @JsonProperty("PAYMENT_AMT")
    private String paymentAmt;

    @JsonProperty("PAYMENT_MIN_AMT")
    private String paymentMinAmt;

    @JsonProperty("INSUR_DURATION")
    private String insurDuration;

    @JsonProperty("PH_NAME")
    private String phName;

    @JsonProperty("INSURED_NAME")
    private String insuredName;

    @JsonProperty("AGENT_CODE")
    private String agentCode;

    @JsonProperty("AGENT_NUM")
    private String agentNum;

    @JsonProperty("AGENT_NAME")
    private String agentName;

//    @JsonProperty("Due_premium")
//    private List<DuePremium> duePremium;
//    @JacksonXmlElementWrapper(localName = "Due_premium")
//    private List<DuePremium> duePremium;

    @JsonProperty("SystemDate")
    private String systemDate;

    @JsonProperty("SystemTime")
    private String systemTime;

    @JsonProperty("MAX_TOPUP")
    private String maxTopup;

    @JsonProperty("MIN_TOPUP")
    private String minTopup;

    @JacksonXmlProperty(localName = "Due_premium")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<DuePremium> duePremiums = new ArrayList<>();

    public List<DuePremium> getDuePremiums() {
        return duePremiums;
    }

    public void setDuePremiums(List<DuePremium> duePremiums) {
        this.duePremiums = duePremiums;
    }


    @Schema
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class DuePremium {
        @JsonProperty("Premium_Type")
        private String premiumType;

        @JsonProperty("Due_From_Date")
        private String dueFromDate;

        @JsonProperty("Due_To_Date")
        private String dueToDate;

        @JsonProperty("Due_Amount")
        private String dueAmount;
    }

}
