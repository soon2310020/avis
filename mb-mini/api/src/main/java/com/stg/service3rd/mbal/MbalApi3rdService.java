package com.stg.service3rd.mbal;

import com.stg.service3rd.mbal.dto.req.FlexibleCheckTSAReq;
import com.stg.service3rd.mbal.dto.req.SubmitPotentialCustomerReq;
import com.stg.service3rd.mbal.dto.resp.FlexibleCheckTSAResp;
import com.stg.service3rd.toolcrm.dto.req.DirectSubmitStatusReq;

import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

@Validated
public interface MbalApi3rdService {

    FlexibleCheckTSAResp checkTSA(FlexibleCheckTSAReq request) throws Exception;

    DirectSubmitStatusReq submitPotentialCustomer(@Valid SubmitPotentialCustomerReq body);

}
