package com.stg.service3rd.mic_sandbox;

import com.stg.service.dto.external.request.MicGenerateInsuranceCertReqDto;
import com.stg.service.dto.external.request.MicInsuranceResultReqDto;
import com.stg.service.dto.external.response.MicGenerateInsuranceCertSandboxRespDto;
import com.stg.service.dto.external.response.MicSandboxContractInfoRespDto;
import com.stg.service.dto.external.response.MicSandboxFeeCareRespDto;
import com.stg.service.dto.external.response.MicSandboxSearchCertRespDto;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;

@Validated
public interface MicSandboxApi3rdService {

    MicSandboxFeeCareRespDto flexibleMicFeeSandboxResult(MicInsuranceResultReqDto reqDto);

    HttpHeaders micAuthHeader();

    MicSandboxContractInfoRespDto micSandboxSearchContractInfo(String contractNum) throws Exception;

    MicSandboxSearchCertRespDto micSandboxSearchCert(String transactionId) throws Exception;

    MicGenerateInsuranceCertSandboxRespDto micSandboxGenerateCert(MicGenerateInsuranceCertReqDto reqDto) throws Exception;

}
