package com.stg.service3rd.baas.thuho;

import com.stg.service3rd.baas.dto.req.CollectBulkPaymentReq;
import com.stg.service3rd.baas.dto.req.RegisterAutoDebitReq;
import com.stg.service3rd.baas.dto.resp.CollectBulkPaymentResp;
import com.stg.service3rd.baas.dto.resp.QueryBulkPaymentResp;
import com.stg.service3rd.baas.dto.resp.RegisterAutoDebitResp;

public interface CollectBulk3rdService {
    RegisterAutoDebitResp registerAutoDebitNonOTP(RegisterAutoDebitReq request) throws Exception;

    CollectBulkPaymentResp collectBulkPayments(CollectBulkPaymentReq request) throws Exception;

    QueryBulkPaymentResp searchBulkPaymentInfo(String bulkId, Integer page, Integer limit) throws Exception;
}
