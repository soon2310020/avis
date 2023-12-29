package com.stg.service;

import com.stg.service.dto.card.GetInstFeeRequest;
import com.stg.service.dto.card.GetInstFeeResponse;
import com.stg.service.dto.external.request.*;
import com.stg.service.dto.external.requestFlexible.FlexibleTransactionReqDto;
import com.stg.service.dto.external.response.*;
import com.stg.service.dto.insurance.FilterContractParam;
import com.stg.service.dto.insurance.InstallmentPaymentPopupDTO;
import com.stg.service.dto.insurance.InsuranceContractDto;
import com.stg.service.dto.insurance.InsuranceContractsAppDto;
import com.stg.utils.CallbackType;
import com.stg.utils.SourceType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ExternalAPIService {
    // Call to MB
    MbCustomerInfoRespDto customerInfo(TokenReqDto infoReqDto) ;

    MbTransactionRespDto miniStartTransaction(String cif, MiniTransactionReqDto reqDto) ;

    MbCallBackTransactionReqDto getTransaction(String transactionId);

    void mbCallBackTransaction(MbCallBackTransactionReqDto reqDto, CallbackType callbackType) ;

    PackageTypeRespDtos packageInfo(String mbalAccessToken, PackageTypeReqDto packageTypeReqDto) ;

    MbalCustomerInfoRespDto mbalCheckCustomerInfo(String cif, String mbalAccessToken, MbalCustomerInfoReqDto mbalCustomerInfoReqDto) ;

    GenerateQuoteRespDto mbalGenerateQuote(String mbalAccessToken, MbalCustomerInfoReqDto mbalGenerateQuoteReqDto) ;

    PackageProductRespDto mbalPackageProduct(String mbalAccessToken, MbalPackageProductReqDto mbalPackageProductReqDto) ;

    List<Object> mbalIllustrationBoardSummary(String mbalAccessToken, MbalIllustrationBoardReqDto illustrationBoardDto) ;

    void mbalSendEmail(String mbalAccessToken, MbalSendEmailReqDto emailReqDto) ;

    AllCategoryRespDto getAllCategory(String mbalAccessToken);

    MbalIllustrationBoardDetailRespDto viewIllustrationBoard(String mbalAccessToken);

    MbalEmployeeSetRespDto employeeSet(String mbalAccessToken, MbalEmployeeSetReqDto reqDto) ;

    MbalValidateBpRespDto validateBp(String mbalAccessToken, MbalCustomerInfoReqDto reqDto) ;

    MbalGenAppNumberRespDto genAppNumber(String mbalAccessToken, MbalCustomerInfoReqDto reqDto) ;

    //MINI_APP
//    PackageTypeRespDtos getPackages(String mbalAccessToken, MiniPackageReqDto reqDto) ;

    List<InsuranceContractDto> getContractsOfCustomer(String mbId, int count, Long lastId);

    List<InsuranceContractsAppDto> getListContractsOfCustomer(FilterContractParam filterParam, int page, int size);

    InsuranceContractDto getDetailContract(String mbId, Long contractId, SourceType source);

    MbalCreateOrderRespDto mbalCreateOrder(String mbalAccessToken, MiniTransactionReqDto reqDto) ;

    ResponseEntity<Void> insertUpdateUl2020Question(String reqDto, MultipartFile[] files, String cookie);

    MbTransactionRespDto flexibleStartTransaction(String cif, FlexibleTransactionReqDto reqDto) ;

    GetInstFeeResponse getInsFee(String cif, GetInstFeeRequest reqDto) ;

    InstallmentPaymentPopupDTO retrieveInstallmentShowPopup(String mbId);

    void confirmInstallmentShowPopup(String mbId, Long contractId);

    void scheduleCheckPendingTransaction();
}
