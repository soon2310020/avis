package com.stg.controller;

import com.stg.entity.user.CustomerIdentifier;
import com.stg.service.ExternalPolicyApiService;
import com.stg.service.dto.external.responseV2.PolicyMbalResponse;
import com.stg.service.dto.insurance.InsuranceContractsAppDto;
import com.stg.service.dto.policy.InquiryPolicySaveReq;
import com.stg.service.dto.policy.InquiryPolicySaveResp;
import com.stg.utils.Endpoints;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@AllArgsConstructor
@Tag(name = "External Policy APIs")
public class ExternalPolicyController {

    private final ExternalPolicyApiService externalPolicyApiService;

    /**
     * Đồng bộ hợp đồng khi lần đầu tiên đăng nhập
     */
    @GetMapping(Endpoints.EXTERNAL_MBAL_POLICY_SYNC)
    @ResponseStatus(HttpStatus.OK)
    public PolicyMbalResponse syncPolicyMbal(@AuthenticationPrincipal CustomerIdentifier identifier) {
        return externalPolicyApiService.syncPolicyMbal(identifier.getMbId());
    }

    /**
     * Truy vấn hợp đồng
     */
    @GetMapping(Endpoints.EXTERNAL_INQUIRY_MBAL_POLICY)
    @ResponseStatus(HttpStatus.OK)
    public InsuranceContractsAppDto inquiryMbalPolicy(@AuthenticationPrincipal CustomerIdentifier identifier,
                                                      @RequestParam(value = "appNumber", required = false, defaultValue = "") String appNumber,
                                                      @RequestParam(value = "policyNumber", required = false, defaultValue = "") String policyNumber) {
        return externalPolicyApiService.inquiryMbalPolicy(identifier.getMbId(), appNumber, policyNumber);
    }

    /**
     * Lưu hợp đồng truy vấn
     */
    @PostMapping(Endpoints.EXTERNAL_INQUIRY_MBAL_POLICY)
    @ResponseStatus(HttpStatus.OK)
    public InquiryPolicySaveResp savePolicyMbal(@AuthenticationPrincipal CustomerIdentifier identifier,
                                                @RequestBody InquiryPolicySaveReq inquiryPolicySaveReq) {
        inquiryPolicySaveReq.setMbId(identifier.getMbId());
        return externalPolicyApiService.savePolicyMbal(inquiryPolicySaveReq);
    }

    // EXTERNAL_CONTRACT_RENEWAL
//    @GetMapping(Endpoints.EXTERNAL_CONTRACT_RENEWAL)
//    @ResponseStatus(HttpStatus.OK)
//    public void listContractRenewal(@AuthenticationPrincipal CustomerIdentifier identifier) {
//        contractService.listContractRenewal();
//    }
}
