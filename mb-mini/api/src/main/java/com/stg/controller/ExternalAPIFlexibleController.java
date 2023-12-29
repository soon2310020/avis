package com.stg.controller;

import com.stg.entity.user.CustomerIdentifier;
import com.stg.service.ExternalAPIService;
import com.stg.service.ExternalFlexibleApiService;
import com.stg.service.ExternalMicAPIService;
import com.stg.service.dto.external.MbalIllustrationDownloadRespDto;
import com.stg.service.dto.external.request.MicPackageReqDto;
import com.stg.service.dto.external.requestFlexible.*;
import com.stg.service.dto.external.response.MbTransactionRespDto;
import com.stg.service.dto.external.response.MicSandboxContractInfoRespDto;
import com.stg.service.dto.external.responseFlexible.FlexibleQuotationResp;
import com.stg.service.dto.external.responseFlexible.FlexibleQuoteRespDto;
import com.stg.service.dto.external.responseFlexible.FlexibleRespModel;
import com.stg.service.dto.external.responseFlexible.PolicyDetailRespDto;
import com.stg.service.dto.external.responseV2.MicInsuranceBenefitV2Dto;
import com.stg.service.dto.insurance.UploadFileRespDto;
import com.stg.service3rd.toolcrm.dto.req.QuotationDetailReq;
import com.stg.utils.Endpoints;
import com.stg.utils.FlexibleCommon;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@Slf4j
@Validated
@RestController
@AllArgsConstructor
@Tag(name = "External Flexible APIs")
public class ExternalAPIFlexibleController {

    private final ExternalMicAPIService micAPIService;
    private final ExternalFlexibleApiService flexibleApiService;
    private final ExternalAPIService externalAPIService;

    @PostMapping(Endpoints.EXTERNAL_FX_MIC_PACKAGES)
    @ResponseStatus(HttpStatus.OK)
    public List<MicInsuranceBenefitV2Dto> retrieveMicPackages(@AuthenticationPrincipal CustomerIdentifier identifier,
                                                              @Valid @RequestBody MicPackageReqDto reqDto) {
        return micAPIService.retrieveMicSandboxPackages(reqDto);
    }

    @PostMapping(Endpoints.APP_FLEXIBLE.FLEX_FIND_DETAIL)
    @ResponseStatus(HttpStatus.OK)
    public FlexibleQuotationResp findQuotationDetail(@AuthenticationPrincipal CustomerIdentifier identifier,
                                                     @Valid @RequestBody QuotationDetailReq reqDto) {
        return flexibleApiService.findQuotationDetail(identifier.getMbId(), reqDto);
    }

    @PostMapping(Endpoints.EXTERNAL_FX_CREATE_PROCESS)
    @ResponseStatus(HttpStatus.OK)
    public FlexibleRespModel createProcess(@AuthenticationPrincipal CustomerIdentifier identifier,
                                           @Valid @RequestBody ProcessFlexibleReqDto reqDto) {
        return flexibleApiService.createProcess(identifier.getMbId(), reqDto);
    }

    @PostMapping(Endpoints.EXTERNAL_FX_ADD_ASSURED)
    @ResponseStatus(HttpStatus.OK)
    public List<FlexibleCommon.Assured> addAssureds(@AuthenticationPrincipal CustomerIdentifier identifier,
                                                    @PathVariable(value = "processId") Integer processId,
                                                    @Valid @RequestBody List<FlexibleCommon.Assured> reqDto) {
        return flexibleApiService.addAssureds(identifier.getMbId(), processId, reqDto);
    }

    @PostMapping(Endpoints.EXTERNAL_FX_CREATE_QUOTATION)
    @ResponseStatus(HttpStatus.OK)
    public FlexibleQuoteRespDto createQuotation(@AuthenticationPrincipal CustomerIdentifier identifier,
                                                @Valid @RequestBody GenerateQuotationDto reqDto) {
        return flexibleApiService.createQuote(identifier.getMbId(), reqDto);
    }

    @PostMapping(Endpoints.EXTERNAL_FX_SEND_MAIL)
    @ResponseStatus(HttpStatus.OK)
    public String sendEmail(@AuthenticationPrincipal CustomerIdentifier identifier,
                            @Valid @RequestBody SendMailFlexibleReqDto reqDto) {
        return flexibleApiService.sendEmail(identifier.getMbId(), reqDto);
    }

    @PostMapping(Endpoints.EXTERNAL_FX_CONFIRM_QUOTATION)
    @ResponseStatus(HttpStatus.OK)
    public FlexibleQuoteRespDto confirmQuote(@AuthenticationPrincipal CustomerIdentifier identifier,
                                             @Valid @RequestBody ConfirmQuoteFlexibleReqDto reqDto) {
        return flexibleApiService.confirmQuote(identifier.getMbId(), reqDto);
    }

    @PostMapping(Endpoints.EXTERNAL_FX_SUBMIT)
    @ResponseStatus(HttpStatus.OK)
    public FlexibleRespModel submitApp(@AuthenticationPrincipal CustomerIdentifier identifier,
                                       @Valid @RequestBody SubmitQuotationDto reqDto) {
        return flexibleApiService.submitApp(identifier.getMbId(), reqDto);
    }

    @GetMapping(Endpoints.EXTERNAL_FX_DOWNLOAD_PDF)
    @ResponseStatus(HttpStatus.OK)
    public MbalIllustrationDownloadRespDto getPDFQuotation(@AuthenticationPrincipal CustomerIdentifier identifier,
                                                           @RequestParam(value = "processId") Integer processId) {
        return flexibleApiService.getPDFQuotation(identifier.getMbId(), processId);
    }

    @PostMapping(Endpoints.EXTERNAL_FX_GET_POLICY)
    @ResponseStatus(HttpStatus.OK)
    public PolicyDetailRespDto mbalPolicyDetail(@AuthenticationPrincipal CustomerIdentifier identifier,
                                                @RequestBody PolicyDetailReqDto reqDto) {
        return flexibleApiService.mbalPolicyDetail(identifier.getMbId(), reqDto);
    }

//    @PostMapping(Endpoints.EXTERNAL_FX_UPLOAD_FILES)
//    @ResponseStatus(HttpStatus.OK)
//    public UploadFileRespDto uploadMultiFileQuestion(@RequestParam("files") MultipartFile[] files,
//                                                     @PathVariable(value = "processId") Integer processId,
//                                                     @AuthenticationPrincipal CustomerIdentifier identifier) {
//        return flexibleApiService.uploadMultiFileQuestion(files, processId, identifier.getMbId());
//    }

    @PostMapping(Endpoints.EXTERNAL_FX_START_TRANSACTION)
    @ResponseStatus(HttpStatus.OK)
    public MbTransactionRespDto flexibleStartTransaction(@AuthenticationPrincipal CustomerIdentifier identifier,
                                                         @Valid @RequestBody FlexibleTransactionReqDto infoReqDto) {
        return externalAPIService.flexibleStartTransaction(identifier.getMbId(), infoReqDto);
    }

    @GetMapping(Endpoints.EXTERNAL_FX_MIC_CONTRACT)
    @ResponseStatus(HttpStatus.OK)
    public MicSandboxContractInfoRespDto micSandboxSearchContractInfo(@AuthenticationPrincipal CustomerIdentifier identifier,
                                                                      @RequestParam(value = "contractNum") String contractNum,
                                                                      @RequestParam(value = "activeDate", required = false, defaultValue = "22/08/2022") String activeDate) {
        return micAPIService.micSandboxSearchContractInfo(contractNum, activeDate, false);
    }

}
