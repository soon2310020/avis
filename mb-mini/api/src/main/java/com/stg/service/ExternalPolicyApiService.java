package com.stg.service;

import com.stg.service.dto.external.responseV2.PolicyMbalResponse;
import com.stg.service.dto.insurance.InsuranceContractsAppDto;
import com.stg.service.dto.policy.InquiryPolicySaveReq;
import com.stg.service.dto.policy.InquiryPolicySaveResp;

public interface ExternalPolicyApiService {

    PolicyMbalResponse syncPolicyMbal(String mbId);

    InsuranceContractsAppDto inquiryMbalPolicy(String mbId, String appNumber, String policyNumber);

    InquiryPolicySaveResp savePolicyMbal(InquiryPolicySaveReq inquiryPolicySaveReq);

}
