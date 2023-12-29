package com.stg.service3rd.mbalv2;

import java.util.List;

import com.stg.service3rd.mbalv2.dto.resp.InquiryPaymentPolicy;

public interface MbalV2Api3rdService {

    List<InquiryPaymentPolicy> getInquiryPaymentPolicies(String id);
}
