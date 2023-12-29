package com.stg.service;

import com.stg.service.dto.external.request.*;
import com.stg.service.dto.external.requestFlexible.MicAdditionalProduct;
import com.stg.service.dto.external.response.*;
import com.stg.service.dto.external.responseV2.MicInsuranceBenefitV2Dto;
import com.stg.utils.Common;
import com.stg.utils.FlexibleCommon;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public interface ExternalMicAPIService {

    // Call to MIC lấy phí
    MicInsuranceResultRespDto micFeeResult(MiniMicFeeReqDto miniMicFeeReqDto);

    // Call to MIC Gen GCNBH
    void micGenerateInsuranceCert(MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh ttinkh,
                                  long micFee, String transactionId, int nhom, Common.GcnMicCareDkbs gcnMicCareDkbs) ;

    // Call to MIC lấy kết quả gen GCNBH
    MicSearchInsuranceCertRespDto micSearchInsuranceCert(String cif, String transactionId) ;

    // Generate request body data cho gói FIX (1-7)
    MiniMicFeeReqDto genMicFeeReqDto(String dob, int nhom);

    MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh generateMicCareTtinkh(String fullname, String idCardNo, String gender,
                                                                          String dob, String address, String email, String phone);

    //Danh sách gói MIC cho flexible V1
    List<MicInsuranceBenefitV2Dto> retrieveMicPackages(MicPackageReqDto reqDto);

    MicInsuranceResultRespDto flexibleMicFeeResult(MiniMicFeeReqDto miniMicFeeReqDto);

    // Call to MIC lấy phí V1
    MicInsuranceResultRespDto flexibleMicFeeResult(FlexibleCommon.Assured assured, MicAdditionalProduct micRequest);

    MicSearchInsuranceCertRespDto flexibleMicInsuranceCert(String transactionId) ;

//    MIC lấy phí sandbox
    MicGetTokenRespDto micSandboxToken();

    MicSandboxFeeCareRespDto flexibleMicSandboxFee(FlexibleCommon.Assured assured, MicAdditionalProduct micRequest);

    MicGenerateInsuranceCertSandboxRespDto micSandboxGenerateCert(MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh ttinkh,
                                                                                     long micFee, String transactionId, int nhom,
                                                                                     Common.GcnMicCareDkbs gcnMicCareDkbs, Common.ParentContract parentContract);

    MicSandboxSearchCertRespDto micSandboxSearchCert(String transactionId);

    MicSandboxFeeCareRespDto flexibleMicFeeSandboxResult(MiniMicFeeReqDto miniMicFeeReqDto);

    //Danh sách gói MIC sandbox cho flexible
    List<MicInsuranceBenefitV2Dto> retrieveMicSandboxPackages(MicPackageReqDto reqDto);

    MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh generateMicSandboxTtinkh(FlexibleCommon.Assured customer,
                                                                             FlexibleCommon.Assured assured);

    MicSandboxContractInfoRespDto micSandboxSearchContractInfo(String contractNum, String activeDate, boolean show);

//    MicGenerateInsuranceCertSandboxRespDto micSandboxGenerateCert(MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh ttinkh,
//                                                                  long micFee, String transactionId, int nhom,
//                                                                  Common.GcnMicCareDkbs gcnMicCareDkbs, Common.ParentContract parentContract);

}
