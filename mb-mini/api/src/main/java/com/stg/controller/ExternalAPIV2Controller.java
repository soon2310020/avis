package com.stg.controller;

import com.stg.entity.user.CustomerIdentifier;
import com.stg.service.ExternalV2ApiService;
import com.stg.service.dto.external.MbalIllustrationDownloadRespDto;
import com.stg.service.dto.external.requestV2.*;
import com.stg.service.dto.external.responseV2.*;
import com.stg.service.dto.insurance.InsuranceContractDto;
import com.stg.service.dto.insurance.InsuranceContractsAppDto;
import com.stg.utils.Endpoints;
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
@Tag(name = "External UL3 APIs")
public class ExternalAPIV2Controller {

    private final ExternalV2ApiService externalV2ApiService;

    @PostMapping(Endpoints.EXTERNAL_MBAL_BMH_CREATE_PROCESS)
    @ResponseStatus(HttpStatus.OK)
    public CreateQuoteV2RespDto mbalCreateProcess(@AuthenticationPrincipal CustomerIdentifier identifier,
                                                  @Valid @RequestBody CreateProcessV2ReqDto reqDto) {
        return externalV2ApiService.mbalCreateProcess(identifier.getMbId(), reqDto);
    }

    @PostMapping(Endpoints.EXTERNAL_MBAL_BMH_CREATE_QUOTE)
    @ResponseStatus(HttpStatus.OK)
    public CreateQuoteV2RespDto mbalCreateQuote(@AuthenticationPrincipal CustomerIdentifier identifier,
                                                @Valid @RequestBody CreateQuoteV2ReqDto reqDto,
                                                @PathVariable(value = "processId") Integer processId) {
        return externalV2ApiService.mbalCreateQuote(identifier.getMbId(), reqDto, processId);
    }

    @PostMapping(Endpoints.EXTERNAL_MBAL_CONFIRM_QUOTATION)
    @ResponseStatus(HttpStatus.OK)
    public CreateQuoteV2RespDto mbalConfirmQuotation(@AuthenticationPrincipal CustomerIdentifier identifier,
                                                     @PathVariable(value = "processId") Integer processId) {
        return externalV2ApiService.mbalConfirmQuotation(identifier.getMbId(), processId);
    }

    @PostMapping(Endpoints.EXTERNAL_MBAL_SUBMIT_APPLICATION)
    @ResponseStatus(HttpStatus.OK)
    public CreateQuoteV2RespDto mbalSubmitApplication(@AuthenticationPrincipal CustomerIdentifier identifier,
                                                      @Valid @RequestBody SubmitApplicationV2ReqDto reqDto,
                                                      @PathVariable(value = "processId") Integer processId) {
        return externalV2ApiService.mbalSubmitApplication(identifier.getMbId(), reqDto, processId);
    }

    @GetMapping(Endpoints.EXTERNAL_MBAL_LIST_OCCUPATION)
    @ResponseStatus(HttpStatus.OK)
    public List<OccupationV2RespDto> mbalListOccupation() {
        return externalV2ApiService.mbalListOccupation();
    }

//    @GetMapping(Endpoints.EXTERNAL_MBAL_LIST_PACKAGE_V2)
//    @ResponseStatus(HttpStatus.OK)
//    public List<PackageV2RespDto> mbalListPackage() {
//        return externalV2ApiService.mbalListPackage();
//    }

    @PostMapping(Endpoints.EXTERNAL_MBAL_SEND_MAIL)
    @ResponseStatus(HttpStatus.OK)
    public String mbalSendMail(@AuthenticationPrincipal CustomerIdentifier identifier,
                               @PathVariable(value = "processId") String processId,
                               @Valid @RequestBody SendMailV2ReqDto v2ReqDto) {
        return externalV2ApiService.mbalSendMail(identifier.getMbId(), processId, v2ReqDto);
    }

//    @PostMapping(Endpoints.EXTERNAL_MBAL_LIST_PACKAGE)
//    @ResponseStatus(HttpStatus.OK)
//    public PackageTypeV2RespDtos getPackages(@AuthenticationPrincipal CustomerIdentifier identifier,
//                                             @Valid @RequestBody MiniPackageV2ReqDto reqDto) {
//        return externalV2ApiService.getPackages(reqDto);
//    }

    @GetMapping(Endpoints.EXTERNAL_MBAL_DOWNLOAD_FILE_BMHL)
    @ResponseStatus(HttpStatus.OK)
    public MbalIllustrationDownloadRespDto downloadFileBMH(@AuthenticationPrincipal CustomerIdentifier identifier,
                                                           @PathVariable(value = "processId") String processId) {
        return externalV2ApiService.downloadFileBMH(processId, identifier.getMbId());
    }

//    @PostMapping(Endpoints.EXTERNAL_MBAL_UPLOAD_MULTI_FILE_QUESTION)
//    @ResponseStatus(HttpStatus.OK)
//    public UploadFileRespDto uploadMultiFileQuestion(@RequestParam("files") MultipartFile[] files,
//                                                     @PathVariable(value = "processId") String processId,
//                                                     @AuthenticationPrincipal CustomerIdentifier identifier) throws IOException, ExecutionException, InterruptedException {
//        return externalV2ApiService.uploadMultiFileQuestion(files, processId, identifier.getMbId());
//    }

//    @PostMapping(Endpoints.EXTERNAL_MBAL_PAYMENT_NOTIFICATION)
//    @ResponseStatus(HttpStatus.OK)
//    public PaymentNotificationV2RespDto paymentNotifications(@Valid @RequestBody PaymentNotificationV2ReqDto paymentNotificationV2ReqDto) {
//        return externalV2ApiService.paymentNotifications(paymentNotificationV2ReqDto);
//    }

    @PostMapping(Endpoints.EXTERNAL_MBAL_LIST_PACKAGE)
    @ResponseStatus(HttpStatus.OK)
    public PackageTypeV2RespDtos getInsurancePackages(@AuthenticationPrincipal CustomerIdentifier identifier,
                                                      @Valid @RequestBody MiniPackageV2ReqDto reqDto) {
        return externalV2ApiService.insurancePackages(reqDto);
    }

}
