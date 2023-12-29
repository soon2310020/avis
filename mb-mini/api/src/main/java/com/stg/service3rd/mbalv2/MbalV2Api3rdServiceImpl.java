package com.stg.service3rd.mbalv2;

import com.stg.service3rd.common.adapter.func.Function;
import com.stg.service3rd.common.adapter.func.IFunction;
import com.stg.service3rd.common.dto.error.IErrorObject;
import com.stg.service3rd.common.dto.soap.SoapRequest;
import com.stg.service3rd.common.exception.Api3rdException;
import com.stg.service3rd.common.utils.ApiUtil;
import com.stg.service3rd.mbalv2.adapter.MbalV2ApiCaller;
import com.stg.service3rd.mbalv2.adapter.MbalV2Functions;
import com.stg.service3rd.mbalv2.dto.req.CalcPaymentEAppNoReq;
import com.stg.service3rd.mbalv2.dto.req.CalcPaymentPolicyReq;
import com.stg.service3rd.mbalv2.dto.req.InquiryPaymentReq;
import com.stg.service3rd.mbalv2.dto.resp.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MbalV2Api3rdServiceImpl implements MbalV2Api3rdService {
    private final MbalV2ApiCaller mbalV2Connector;

    final Map<String, String> paramPrefix;

    @Autowired
    public MbalV2Api3rdServiceImpl(MbalV2ApiCaller mbalV2Connector, @Value("${api3rd.mbal_v2.endpoint_prefix}") String endpointPrefix) {
        this.mbalV2Connector = mbalV2Connector;
        this.paramPrefix = Map.of("endpoint", endpointPrefix);
    }

    @Override
    public List<InquiryPaymentPolicy> getInquiryPaymentPolicies(String id) {
        try {
            SoapRequest request = new SoapRequest();
            InquiryPaymentReq body = new InquiryPaymentReq();
            body.getInput().setType("ZMBL02"); // ZMBL02 vs POLICY
            body.getInput().setNumber(id);
            request.setBody(body);

            IFunction reqUri = Function.map(MbalV2Functions.POLICY_DETAIL_V2, this.paramPrefix);
            InquiryPaymentResp resp = mbalV2Connector.post(reqUri, request, InquiryPaymentResp.class);
            return resp.getBody().getOuput().getPolicies();
        } catch (Exception e) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(e);
            throw new Api3rdException(errorObject.getErrorMessage(), errorObject.toErrorDto());
        }
    }

    @Override
    public CalcPaymentResp getCalcPaymentPolicies(String id) {
        try {
            SoapRequest request = new SoapRequest();
            CalcPaymentPolicyReq body = new CalcPaymentPolicyReq();
            body.getInput().setNumber(id);
            request.setBody(body);

            IFunction reqUri = Function.map(MbalV2Functions.CALC_POLICY_V2, this.paramPrefix);
            return mbalV2Connector.post(reqUri, request, CalcPaymentResp.class);
        } catch (Exception e) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(e);
            throw new Api3rdException(errorObject.getErrorMessage(), errorObject.toErrorDto());
        }
    }

    @Override
    public CalcPaymentEAppResp getCalcPaymentEApp(String eAppNo) throws Exception {
        try {
            SoapRequest request = new SoapRequest();
            CalcPaymentEAppNoReq body = new CalcPaymentEAppNoReq();
            body.getInput().setInquirySearch(eAppNo);
            request.setBody(body);

            IFunction reqUri = Function.map(MbalV2Functions.CALC_EAPP_V2, this.paramPrefix);
            return mbalV2Connector.post(reqUri, request, CalcPaymentEAppResp.class);
        } catch (Exception e) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(e);
            throw new Api3rdException(errorObject.getErrorMessage(), errorObject.toErrorDto());
        }
    }

}
