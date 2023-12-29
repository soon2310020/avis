package com.stg.service.dto.external.responseV2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PolicySoapDTO {
    private String policyNum;
    private String policyStatus;
    private String policyStatusId;
    private String prdName;
    private String phName;
    private String payFrequency;
    private String currency;
    private String periodicPrem;
    private String agentCode;
    private String agentNum;
    private String agentName;
}
