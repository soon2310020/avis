package com.stg.service3rd.toolcrm;

import com.stg.service3rd.toolcrm.dto.req.QuotationDetailReq;
import com.stg.service3rd.toolcrm.dto.req.QuotationSyncDataReq;
import com.stg.service3rd.toolcrm.dto.resp.QuotationDetailResp;
import com.stg.service3rd.toolcrm.dto.resp.QuotationSyncDataResp;

public interface ToolCrmApi3rdService {
    QuotationDetailResp findQuotationDetail(QuotationDetailReq request);

    /**
     * NOTE: state (action) => tool service auto detect!
     */
    QuotationSyncDataResp syncData(QuotationSyncDataReq request);

}
