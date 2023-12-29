package com.stg.controller;

import com.stg.entity.user.CustomerIdentifier;
import com.stg.errors.ValidationException;
import com.stg.service.ExternalAPIService;
import com.stg.service.ExternalMicAPIService;
import com.stg.service.dto.card.GetInstFeeRequest;
import com.stg.service.dto.card.GetInstFeeResponse;
import com.stg.service.dto.external.request.*;
import com.stg.service.dto.external.response.*;
import com.stg.service.dto.insurance.FilterContractParam;
import com.stg.service.dto.insurance.InsuranceContractDto;
import com.stg.service.dto.insurance.InsuranceContractsAppDto;
import com.stg.utils.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

import static com.stg.utils.CommonMessageError.MSG40;
import static com.stg.utils.Constants.MBAL_ACCESS_TOKEN_HEADER;
import static com.stg.utils.Endpoints.EXTERNAL_INSTALLMENT_FEE_CHECK;
import static com.stg.utils.Endpoints.MINI_INSTALLMENT_CONFIRM;


@Slf4j
@Validated
@RestController
@AllArgsConstructor
@Tag(name = "External APIs")
public class ExternalAPIController {

    private final ExternalAPIService externalAPIService;
    private final ExternalMicAPIService micAPIService;

    @PostMapping(Endpoints.EXTERNAL_MB_SESSION_VERIFY)
    @ResponseStatus(HttpStatus.OK)
    public MbCustomerInfoRespDto customerInfo(@Valid @RequestBody TokenReqDto infoReqDto) {
        return externalAPIService.customerInfo(infoReqDto);
    }

    @PostMapping(Endpoints.EXTERNAL_MB_START_TRANSACTION)
    @ResponseStatus(HttpStatus.OK)
    public MbTransactionRespDto startTransaction(@AuthenticationPrincipal CustomerIdentifier identifier,
                                                 @Valid @RequestBody MiniTransactionReqDto infoReqDto) {
        return externalAPIService.miniStartTransaction(identifier.getMbId(), infoReqDto);
    }

    @PostMapping(Endpoints.EXTERNAL_MB_CALLBACK_TRANSACTION)
    @ResponseStatus(HttpStatus.OK)
    public void mbCallBackTransaction(@Valid @RequestBody MbCallBackTransactionReqDto reqDto) {
        externalAPIService.mbCallBackTransaction(reqDto, CallbackType.AUTO);
    }

    @PostMapping(Endpoints.EXTERNAL_MBAL_PACKAGE_INFO)
    @ResponseStatus(HttpStatus.OK)
    public PackageTypeRespDtos packageInfo(@RequestHeader(value = MBAL_ACCESS_TOKEN_HEADER) String mbalAccessToken,
                                           @Valid @RequestBody PackageTypeReqDto packageTypeReqDto) {
        return externalAPIService.packageInfo(mbalAccessToken, packageTypeReqDto);
    }

    @PostMapping(Endpoints.EXTERNAL_MBAL_CUSTOMER_INFO)
    @ResponseStatus(HttpStatus.OK)
    public MbalCustomerInfoRespDto checkMbalCustomerInfo(@AuthenticationPrincipal CustomerIdentifier identifier,
                                                         @RequestHeader(value = MBAL_ACCESS_TOKEN_HEADER) String mbalAccessToken,
                                                         @Valid @RequestBody MbalCustomerInfoReqDto mbalCustomerInfoReqDto) {
        return externalAPIService.mbalCheckCustomerInfo(identifier.getMbId(), mbalAccessToken, mbalCustomerInfoReqDto);
    }

//    @PostMapping(Endpoints.EXTERNAL_MBAL_GENERATE_QUOTE)
//    @ResponseStatus(HttpStatus.OK)
//    public GenerateQuoteRespDto mbalGenerateQuote(@RequestHeader(value = MBAL_ACCESS_TOKEN_HEADER) String mbalAccessToken,
//                                                  @Valid @RequestBody MbalCustomerInfoReqDto mbalGenerateQuoteReqDto) {
//        return externalAPIService.mbalGenerateQuote(mbalAccessToken, mbalGenerateQuoteReqDto);
//    }

    @PostMapping(Endpoints.EXTERNAL_MBAL_PACKAGE_PRODUCT)
    @ResponseStatus(HttpStatus.OK)
    public PackageProductRespDto mbalPackageProduct(@RequestHeader(value = MBAL_ACCESS_TOKEN_HEADER) String mbalAccessToken,
                                                    @Valid @RequestBody MbalPackageProductReqDto mbalPackageProductReqDto) {
        return externalAPIService.mbalPackageProduct(mbalAccessToken, mbalPackageProductReqDto);
    }

    @PostMapping(Endpoints.EXTERNAL_MBAL_BMH_SUMMARY)
    @ResponseStatus(HttpStatus.OK)
    public List<Object> mbalBmhSummury(@RequestHeader(value = MBAL_ACCESS_TOKEN_HEADER) String mbalAccessToken,
                                       @Valid @RequestBody MbalIllustrationBoardReqDto illustrationBoardDto) {
        return externalAPIService.mbalIllustrationBoardSummary(mbalAccessToken, illustrationBoardDto);
    }

    @PostMapping(Endpoints.EXTERNAL_MBAL_SEND_EMAIL)
    @ResponseStatus(HttpStatus.OK)
    public void mbalSendEmail(@RequestHeader(value = MBAL_ACCESS_TOKEN_HEADER) String mbalAccessToken,
                              @Valid @RequestBody MbalSendEmailReqDto emailReqDto) {
        externalAPIService.mbalSendEmail(mbalAccessToken, emailReqDto);
    }

    @GetMapping(Endpoints.EXTERNAL_MBAL_ALL_CATEGORY)
    @ResponseStatus(HttpStatus.OK)
    public AllCategoryRespDto getAllCategory(@RequestHeader(value = MBAL_ACCESS_TOKEN_HEADER) String mbalAccessToken) {
        return externalAPIService.getAllCategory(mbalAccessToken);
    }

    @PostMapping(Endpoints.EXTERNAL_MBAL_VIEW_ILLUSTRATION_BOARD)
    @ResponseStatus(HttpStatus.OK)
    public MbalIllustrationBoardDetailRespDto viewIllustrationBoard(@RequestHeader(value = MBAL_ACCESS_TOKEN_HEADER) String mbalAccessToken) {
        return externalAPIService.viewIllustrationBoard(mbalAccessToken);
    }

    //MIC
    @PostMapping(Endpoints.EXTERNAL_MIC_INSURANCE_RESULT)
    @ResponseStatus(HttpStatus.OK)
    public MicInsuranceResultRespDto getMicInsuranceResult(@Valid @RequestBody MiniMicFeeReqDto miniMicFeeReqDto) {
        return micAPIService.micFeeResult(miniMicFeeReqDto);
    }

    @GetMapping(Endpoints.EXTERNAL_MIC_SEARCH_INSURANCE_CERT)
    @ResponseStatus(HttpStatus.OK)
    public MicSearchInsuranceCertRespDto micSearchInsuranceCert(@AuthenticationPrincipal CustomerIdentifier identifier,
                                                                @RequestParam(value = "transactionId") String transactionId) {
        return micAPIService.micSearchInsuranceCert(identifier.getMbId(), transactionId);
    }

    // MINI_APP
//    @PostMapping(Endpoints.MINI_APP_PACKAGES)
//    @ResponseStatus(HttpStatus.OK)
//    public PackageTypeRespDtos getPackages(@RequestHeader(value = MBAL_ACCESS_TOKEN_HEADER) String mbalAccessToken,
//                                           @Valid @RequestBody MiniPackageReqDto reqDto) {
//        return externalAPIService.getPackages(mbalAccessToken, reqDto);
//    }

//    @GetMapping(Endpoints.MINI_APP_CONTRACTS)
//    @ResponseStatus(HttpStatus.OK)
//    public List<InsuranceContractDto> getContractsOfCustomer(@AuthenticationPrincipal CustomerIdentifier identifier,
//                                                             @RequestParam(value = "count", required = false, defaultValue = "10") int count,
//                                                             @RequestParam(value = "lastId", required = false, defaultValue = "0") Long lastId) {
//        return externalAPIService.getContractsOfCustomer(identifier.getMbId(), count, lastId);
//    }

    @GetMapping(Endpoints.MINI_APP_LIST_CONTRACTS)
    @ResponseStatus(HttpStatus.OK)
    public List<InsuranceContractsAppDto> getListContractsOfCustomer(@AuthenticationPrincipal CustomerIdentifier identifier,
                                                                     @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                                                     @RequestParam(name = "size", required = false, defaultValue = "20") int size,
                                                                     @RequestParam(value = "status", defaultValue = "EMPTY") ContractStatusType contractStatus,
                                                                     @RequestParam(value = "renewalStatus", required = false, defaultValue = "") DuePremiumType renewalStatus,
                                                                     @RequestParam(value = "dueDateFrom", required = false, defaultValue = "") LocalDate dueDateFrom,
                                                                     @RequestParam(value = "dueDateTo", required = false, defaultValue = "") String dueDateTo,
                                                                     @RequestParam(value = "query", required = false, defaultValue = "") String query,
                                                                     @RequestParam(value = "source", required = false, defaultValue = "MB") SourceType source) {
        if (StringUtils.isEmpty(identifier.getMbId())) {
            throw new ValidationException(MSG40);
        }
        FilterContractParam filterParam = new FilterContractParam(identifier.getMbId(), query, contractStatus, renewalStatus, dueDateFrom, dueDateTo, source);
        return externalAPIService.getListContractsOfCustomer(filterParam, page, size);
    }

    @GetMapping(Endpoints.MINI_APP_DETAIL_CONTRACT)
    @ResponseStatus(HttpStatus.OK)
    public InsuranceContractDto getDetailContract(@AuthenticationPrincipal CustomerIdentifier identifier,
                                                  @PathVariable(value = "contractId") Long contractId,
                                                  @RequestParam(value = "source", required = false, defaultValue = "MB") SourceType source) {
        return externalAPIService.getDetailContract(identifier.getMbId(), contractId, source);
    }

    @GetMapping(Endpoints.MINI_APP_INSERT_UPDATE_UL2020_QUESTION)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> insertUpdateUl2020Question(@RequestParam("input") String reqDto,
                                                           @RequestParam("files") MultipartFile[] files,
                                                           @RequestHeader(value = "Cookie") String cookie) {
        return externalAPIService.insertUpdateUl2020Question(reqDto, files, cookie);
    }

    @PostMapping(EXTERNAL_INSTALLMENT_FEE_CHECK)
    public ResponseEntity<GetInstFeeResponse> getInsFee(@AuthenticationPrincipal CustomerIdentifier identifier,
                                                        @Valid @RequestBody GetInstFeeRequest infoReqDto) {
        return new ResponseEntity<>(externalAPIService.getInsFee(identifier.getMbId(), infoReqDto), HttpStatus.OK);
    }

    @PutMapping(MINI_INSTALLMENT_CONFIRM)
    @ResponseStatus(HttpStatus.OK)
    public void confirmInstallmentShowPopup(@AuthenticationPrincipal CustomerIdentifier identifier,
                                            @RequestParam(value = "paymentId") Long contractId) {
        externalAPIService.confirmInstallmentShowPopup(identifier.getMbId(), contractId);
    }

}
