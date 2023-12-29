package com.stg.service3rd.toolcrm;

import static com.stg.service3rd.common.utils.ApiUtil.SERVER_ERROR_MESSAGE;

import java.util.Map;

import com.stg.service3rd.common.dto.error.IErrorObject;
import com.stg.service3rd.common.utils.ApiUtil;
import com.stg.service3rd.toolcrm.dto.error.ToolCrmError;
import org.springframework.stereotype.Service;

import com.stg.config.jackson.Jackson;
import com.stg.service3rd.toolcrm.adapter.ToolCrmApiCaller;
import com.stg.service3rd.toolcrm.adapter.ToolCrmFunctions;
import com.stg.service3rd.toolcrm.dto.req.QuotationDetailReq;
import com.stg.service3rd.toolcrm.dto.req.QuotationSyncDataReq;
import com.stg.service3rd.toolcrm.dto.resp.QuotationDetailResp;
import com.stg.service3rd.toolcrm.dto.resp.QuotationSyncDataResp;
import com.stg.service3rd.toolcrm.error.ToolCrmApi3rdException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ToolCrmApi3rdServiceImpl implements ToolCrmApi3rdService {
    private final ToolCrmApiCaller toolCrmCaller;
    private final Jackson jackson = Jackson.snack();

    public ToolCrmApi3rdServiceImpl(ToolCrmApiCaller toolCrmCaller) {
        this.toolCrmCaller = toolCrmCaller;
    }

    @Override
    public QuotationDetailResp findQuotationDetail(QuotationDetailReq request) {
        try {
            String body = toolCrmCaller.get(ToolCrmFunctions.FIND_QUOTE_DETAIL, String.class, Map.of("uuid", request.getQuotationUid()));
            return jackson.fromJson(body, QuotationDetailResp.class);
        } catch (Exception e) {
            IErrorObject error = ApiUtil.parseErrorMessage(e, ToolCrmError.class);
            throw new ToolCrmApi3rdException(SERVER_ERROR_MESSAGE, error.toErrorDto());
        }
    }

    @Override
    public QuotationSyncDataResp syncData(QuotationSyncDataReq request) {
        try {
            return toolCrmCaller.post(ToolCrmFunctions.SYN_DATA, request, QuotationSyncDataResp.class);
        } catch (Exception e) {
            IErrorObject error = ApiUtil.parseErrorMessage(e, ToolCrmError.class);
            throw new ToolCrmApi3rdException(SERVER_ERROR_MESSAGE, error.toErrorDto());
        }
    }

}
