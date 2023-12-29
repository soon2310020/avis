package com.stg.service.dto.policy;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class InquiryPolicySaveReq {
    private String mbId;

    private String policyNumber = "";
    private String appNumber = "";
}
