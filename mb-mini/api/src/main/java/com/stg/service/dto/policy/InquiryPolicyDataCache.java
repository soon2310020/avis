package com.stg.service.dto.policy;

import com.stg.service3rd.mbalv2.dto.resp.CalcPaymentPolicy;
import com.stg.service3rd.mbalv2.dto.resp.InquiryPaymentPolicy;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class InquiryPolicyDataCache {

    private InquiryPaymentPolicy inquiryPolicy;

    private CalcPaymentPolicy policy;

}
