package com.stg.service3rd.mbalv2;

import com.stg.service3rd.common.adapter.func.Function;
import com.stg.service3rd.common.adapter.func.IFunction;
import com.stg.service3rd.common.dto.error.IErrorObject;
import com.stg.service3rd.common.dto.soap.SoapRequest;
import com.stg.service3rd.common.exception.Api3rdException;
import com.stg.service3rd.common.utils.ApiUtil;
import com.stg.service3rd.mbalv2.adapter.MbalV2ApiCaller;
import com.stg.service3rd.mbalv2.adapter.MbalV2Functions;
import com.stg.service3rd.mbalv2.dto.req.InquiryPaymentReq;
import com.stg.service3rd.mbalv2.dto.resp.InquiryPaymentPolicy;
import com.stg.service3rd.mbalv2.dto.resp.InquiryPaymentResp;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MbalV2Api3rdServiceImpl implements MbalV2Api3rdService {
    private final MbalV2ApiCaller mbalV2Connector;

    @Value("${api3rd.mbal_v2.endpoint_prefix}")
    private String endpointPrefix;

    @Override
    public List<InquiryPaymentPolicy> getInquiryPaymentPolicies(String id) {
        try {
            SoapRequest request = new SoapRequest();
            InquiryPaymentReq body = new InquiryPaymentReq();
            body.getInput().setType("ZMBL02");
            body.getInput().setNumber(id);
            request.setBody(body);

            IFunction reqUri = Function.map(MbalV2Functions.POLICY_DETAIL_V2, Map.of("endpoint", endpointPrefix));
            InquiryPaymentResp resp = mbalV2Connector.post(reqUri, request, InquiryPaymentResp.class);
            return resp.getBody().getOuput().getPolicies();
        } catch (Exception e) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(e);
            if ("203".equals(errorObject.getErrorCode())) {
                return List.of();
            }
            throw new Api3rdException(errorObject.getErrorMessage(), errorObject.toErrorDto());
        }
    }

}
