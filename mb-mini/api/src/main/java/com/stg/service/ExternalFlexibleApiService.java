package com.stg.service;

import com.stg.entity.InsuranceRequest;
import com.stg.service.dto.external.MbalIllustrationDownloadRespDto;
import com.stg.service3rd.toolcrm.dto.req.QuotationDetailReq;
import com.stg.service.dto.external.requestFlexible.*;
import com.stg.service.dto.external.responseFlexible.*;
import com.stg.service.dto.insurance.UploadFileRespDto;
import com.stg.utils.FlexibleCommon;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ExternalFlexibleApiService {

    FlexibleQuotationResp findQuotationDetail(String cif, QuotationDetailReq reqDto);

    FlexibleRespModel createProcess(String cif, ProcessFlexibleReqDto reqDto);

    List<FlexibleCommon.Assured> addAssureds(String cif, Integer processId, List<FlexibleCommon.Assured> reqDto);

    FlexibleQuoteRespDto createQuote(String cif, GenerateQuotationDto reqDto);

    String sendEmail(String cif, SendMailFlexibleReqDto reqDto);

    FlexibleQuoteRespDto confirmQuote(String cif, ConfirmQuoteFlexibleReqDto reqDto);

    FlexibleRespModel submitApp(String cif, SubmitQuotationDto reqDto);

    PaymentNotificationFlexibleRespDto paymentNotification(PaymentNotificationFlexibleReqDto reqDto);

    MbalIllustrationDownloadRespDto getPDFQuotation(String cif, Integer processId);

    PolicyDetailRespDto mbalPolicyDetail(String cif, PolicyDetailReqDto reqDto);

    InsuranceRequest savingInsuranceRequestAndIllustration(String cif, Integer processId, FlexibleQuoteRespDto quoteRespDto,
                                                           String insuranceRequestIdCache, List<FlexibleSubmitQuestionRequest> healths);

    UploadFileRespDto uploadMultiFileQuestion(MultipartFile[] files, Integer processId, String cif);
}
