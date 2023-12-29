package com.stg.service3rd.mbalv2;

import com.stg.service3rd.mbalv2.dto.resp.CalcPaymentEAppResp;
import com.stg.service3rd.mbalv2.dto.resp.CalcPaymentResp;
import com.stg.service3rd.mbalv2.dto.resp.InquiryPaymentPolicy;

import java.util.List;

public interface MbalV2Api3rdService {

    List<InquiryPaymentPolicy> getInquiryPaymentPolicies(String id);

    CalcPaymentResp getCalcPaymentPolicies(String id) throws Exception;

    CalcPaymentEAppResp getCalcPaymentEApp(String eAppNo) throws Exception;

}
