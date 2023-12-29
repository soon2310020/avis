package com.stg.service3rd.mbalv2.dto.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class InquiryPaymentPolicy {
    @JsonProperty("POLICY_NUM")
    private String policyNum;
    @JsonProperty("POLICY_STATUS")
    private String policyStatus;
    @JsonProperty("PRD_NAME")
    private String prdName;
    @JsonProperty("PH_NAME")
    private String phName;
    @JsonProperty("PAY_FREQUENCY")
    private String payFrequency;
    @JsonProperty("CURRENCY")
    private String currency;
    @JsonProperty("PERIODIC_PREM")
    private String periodicPrem;
    @JsonProperty("POLICY_STATUS_ID")
    private String policyStatusId;
    @JsonProperty("AGENT_CODE")
    private String agentCode;
    @JsonProperty("AGENT_NUM")
    private String agentNum;
    @JsonProperty("AGENT_NAME")
    private String agentName;
}
