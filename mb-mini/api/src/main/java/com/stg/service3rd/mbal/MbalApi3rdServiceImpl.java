package com.stg.service3rd.mbal;

import com.stg.service3rd.common.dto.error.ErrorDesc;
import com.stg.service3rd.common.dto.error.IErrorObject;
import com.stg.service3rd.common.utils.ApiUtil;
import com.stg.service3rd.mbal.adapter.MbalApiCaller;
import com.stg.service3rd.mbal.adapter.MbalFunctions;
import com.stg.service3rd.mbal.dto.req.FlexibleCheckTSAReq;
import com.stg.service3rd.mbal.dto.req.SubmitPotentialCustomerReq;
import com.stg.service3rd.mbal.dto.resp.FlexibleCheckTSAResp;
import com.stg.service3rd.mbal.dto.resp.SubmitPotentialCustomerResp;
import com.stg.service3rd.toolcrm.constant.DirectSubmitStatus;
import com.stg.service3rd.toolcrm.dto.req.DirectSubmitStatusReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MbalApi3rdServiceImpl implements MbalApi3rdService {
    private final MbalApiCaller mbalConnector;

    /*@Value("${api3rd.mbal.refer-direct.client_id}")
    private String referDirectClientId;
    @Value("${api3rd.mbal.refer-direct.client_secret}")
    private String referDirectClientSecret;*/

    @Override
    public FlexibleCheckTSAResp checkTSA(FlexibleCheckTSAReq request) throws Exception {
        return mbalConnector.post(MbalFunctions.FLEX_CHECK_TSA, request, FlexibleCheckTSAResp.class);
    }

    /**
     * Remove @Valid => log into third_party_log
     */
    @Override
    public DirectSubmitStatusReq submitPotentialCustomer(SubmitPotentialCustomerReq body) {
        try {
            /*HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(referDirectClientId, referDirectClientSecret);
            headers.setContentType(APPLICATION_JSON);*/

            mbalConnector.post(MbalFunctions.SUBMIT_POTENTIAL_CUSTOMER, body, SubmitPotentialCustomerResp.class);
            return DirectSubmitStatusReq.of(body, DirectSubmitStatus.SUCCESS);
        } catch (Exception e) {
            log.error("[MINI]--Lỗi gửi thông tin KHTN với detail error {}", e);
            IErrorObject error = ApiUtil.parseErrorMessage(e, ErrorDesc.class);
            DirectSubmitStatusReq req = DirectSubmitStatusReq.of(body, DirectSubmitStatus.ERROR);
            req.setErrorHttpCode(error.getHttpStatus());
            req.setErrorMessage(error.getErrorMessage());
            return req;
        }
    }

}
