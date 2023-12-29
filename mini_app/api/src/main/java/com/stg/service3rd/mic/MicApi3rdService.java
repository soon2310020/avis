package com.stg.service3rd.mic;

import com.stg.service.dto.mic.MiniMicFeeReqDto;
import com.stg.service3rd.mbal.dto.*;
import com.stg.service3rd.mic.dto.req.MicGenerateInsuranceCertReqDto;
import com.stg.service3rd.mic.dto.resp.MicGenerateInsuranceCertRespDto;
import com.stg.service3rd.mic.dto.resp.MicSandboxSearchCertRespDto;

//import java.util.concurrent.CompletableFuture;

public interface MicApi3rdService {
    MicGenerateInsuranceCertRespDto generateInsuranceCert(MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh ttinkh,
                                                          long micFee, String transactionId, int nhom,
                                                          FlexibleCommon.GcnMicCareDkbs gcnMicCareDkbs) throws Exception;

    MicInsuranceResultRespDto flexibleMicFeeResult(MiniMicFeeReqDto miniMicFeeReqDto) throws Exception;

    MicInsuranceResultRespDto flexibleMicFeeResult(FlexibleCommon.Assured assured, MicAdditionalProduct micRequest) throws Exception;

    // MIC_SANDBOX
    MicGetTokenRespDto micSandboxToken();

    MicSandboxFeeCareRespDto flexibleMicSandboxFee(FlexibleCommon.Assured assured, MicAdditionalProduct micRequest);

    MicGenerateInsuranceCertSandboxRespDto micSandboxGenerateCert(MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh ttinkh,
                                                                  long micFee, String transactionId, int nhom,
                                                                  FlexibleCommon.GcnMicCareDkbs gcnMicCareDkbs, FlexibleCommon.ParentContract parentContract);

    MicSandboxSearchCertRespDto micSandboxSearchCert(String transactionId);

    MicSandboxFeeCareRespDto flexibleMicFeeSandboxResult(MiniMicFeeReqDto miniMicFeeReqDto);

    MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh generateMicSandboxTtinkh(FlexibleCommon.Assured customer,
                                                                             FlexibleCommon.Assured assured);

    MicSandboxContractInfoRespDto micSandboxSearchContractInfo(String contractNum, String activeDate);
}
