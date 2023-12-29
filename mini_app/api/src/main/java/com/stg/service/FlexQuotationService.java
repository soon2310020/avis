package com.stg.service;

import com.stg.service.dto.mbal.ConfirmQuoteFlexibleReqDto;
import com.stg.service.dto.quotation.GenQRCodeResp;
import com.stg.service.dto.quotation.QuotationDto;
import com.stg.service.dto.quotation.QuotationHeaderDto;
import com.stg.service.dto.quotation.QuotationSyncDataDto;
import com.stg.service.dto.quotation.QuotationSyncDataResp;
import com.stg.service.dto.quotation.UpdateQuoteAndGenQRCodeReq;
import com.stg.service3rd.mbal.dto.req.FlexibleProcessReq;
import com.stg.service3rd.mbal.dto.resp.FlexibleRespModel;

import java.util.UUID;

public interface FlexQuotationService {
    FlexibleRespModel flexCreateProcess(FlexibleProcessReq reqDto);

    QuotationDto flexCreateQuote(QuotationHeaderDto reqDto);

    QuotationDto flexConfirmQuote(ConfirmQuoteFlexibleReqDto reqDto);

    GenQRCodeResp updateAndGenQRCode(UpdateQuoteAndGenQRCodeReq reqDto);

    QuotationDto findQuotationDetail(UUID uuid);

    QuotationSyncDataResp syncQuotation(QuotationSyncDataDto request);
}
