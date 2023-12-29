package com.stg.service3rd.mic_sandbox;

import com.stg.errors.MicApiException;
import com.stg.service.dto.external.request.MicGenerateInsuranceCertReqDto;
import com.stg.service.dto.external.request.MicGetTokenRespDto;
import com.stg.service.dto.external.request.MicInsuranceResultReqDto;
import com.stg.service.dto.external.response.MicGenerateInsuranceCertSandboxRespDto;
import com.stg.service.dto.external.response.MicSandboxContractInfoRespDto;
import com.stg.service.dto.external.response.MicSandboxFeeCareRespDto;
import com.stg.service.dto.external.response.MicSandboxSearchCertRespDto;
import com.stg.service3rd.common.dto.error.IErrorObject;
import com.stg.service3rd.common.utils.ApiUtil;
import com.stg.service3rd.mic_sandbox.adapter.MicSandboxApiCaller;
import com.stg.service3rd.mic_sandbox.dto.error.MicSandboxError;
import com.stg.service3rd.mic_sandbox.exception.MicApi3rdException;
import com.stg.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

import static com.stg.service3rd.mic_sandbox.adapter.MicSandboxFunctions.*;
import static com.stg.utils.Common.MERCHANT;
import static com.stg.utils.CommonMessageError.MIC_MSG01;
import static com.stg.utils.CommonMessageError.MIC_MSG02;
import static com.stg.utils.CommonMessageError.MIC_MSG05;
import static com.stg.utils.Constants.MIC_CODE_02;
import static com.stg.utils.MicUtil.MIC_ERROR_CONTRACT;
import static com.stg.utils.MicUtil.MIC_EXPIRED;
import static com.stg.utils.MicUtil.MIC_NOT_EXACTLY;

@Slf4j
@Service
@RequiredArgsConstructor
public class MicSandboxApi3rdServiceImpl implements MicSandboxApi3rdService {

    private final MicSandboxApiCaller micSandboxApiCaller;

    @Override
    public MicSandboxFeeCareRespDto flexibleMicFeeSandboxResult(MicInsuranceResultReqDto reqDto) {
        try {
            MicSandboxFeeCareRespDto respDto = micSandboxApiCaller.post(MIC_FEE_CARE, micAuthHeader(),
                    reqDto, MicSandboxFeeCareRespDto.class);
            if (respDto != null && MIC_CODE_02.equals(respDto.getCode())) {
                throw new MicApiException(respDto.getMessage());
            }
            return respDto;
        } catch (Exception e) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(e, MicSandboxError.class);

            if (errorObject.getErrorMessage().contains(MIC_ERROR_CONTRACT)) {
                throw new MicApiException(String.format(MIC_MSG02, reqDto.getTtin_hd_bme().getSo_hd_bm(), "30%"));
            } else if (errorObject.getErrorMessage().contains(MIC_NOT_EXACTLY) || errorObject.getErrorMessage().contains(MIC_EXPIRED)) {
                throw new MicApiException(String.format(MIC_MSG01, reqDto.getTtin_hd_bme().getSo_hd_bm()));
            }
            throw new MicApi3rdException(errorObject.getErrorMessage(), errorObject.toErrorDto());
        }
    }

    @Override
    public HttpHeaders micAuthHeader() {
        try {
            MicGetTokenRespDto micToken = micSandboxApiCaller.post(MIC_GET_TOKEN, micSandboxApiCaller.getAuthHeader(),
                    micSandboxApiCaller.genMicGetTokenReq(), MicGetTokenRespDto.class);
            if (micToken != null && micToken.getData() != null) {
                HttpHeaders authHeader = micSandboxApiCaller.getAuthHeader();
                authHeader.set(MERCHANT, micToken.getData().getMerchant());
                authHeader.setBearerAuth(micToken.getData().getAccess_token());
                return authHeader;
            }
            log.error("[MIC]--Lỗi xác thực MIC");
            throw new MicApiException("Lỗi xác thực MIC");
        } catch (Exception e) {
            throw new MicApiException("Lỗi xác thực MIC");
        }
    }

    @Override
    public MicSandboxContractInfoRespDto micSandboxSearchContractInfo(String contractNum) throws Exception {
        return micSandboxApiCaller.get(MIC_SEARCH_CONTRACT_SANDBOX, micAuthHeader(),
                MicSandboxContractInfoRespDto.class, Map.of("so_hd", contractNum, "ngay_hl",
                        DateUtil.localDateTimeToString(DateUtil.DATE_DMY, LocalDateTime.now())));
    }

    @Override
    public MicSandboxSearchCertRespDto micSandboxSearchCert(String transactionId) throws Exception {
        return micSandboxApiCaller.get(MIC_SEARCH_INSURANCE_CERT_SANDBOX, micAuthHeader(),
                MicSandboxSearchCertRespDto.class, Map.of("id_trans", transactionId));
    }

    @Override
    public MicGenerateInsuranceCertSandboxRespDto micSandboxGenerateCert(MicGenerateInsuranceCertReqDto reqDto) {
        try {
            MicGenerateInsuranceCertSandboxRespDto respDto = micSandboxApiCaller.post(MIC_GENERATE_INSURANCE_CERT_SANDBOX,
                    micAuthHeader(), reqDto, MicGenerateInsuranceCertSandboxRespDto.class);

            if (Objects.isNull(respDto)) {
                log.error("[MIC]--Lỗi không tạo được GCNBH Sandbox MIC. Input {}. Response {}", reqDto, null);
                throw new MicApiException(MIC_MSG05);
            }

            if (MIC_CODE_02.equals(respDto.getCode())) { // tạo không thành công
                log.error("[MIC]--Lỗi khi GEN GCNBH Sandbox MIC. Input {}. Response {}", reqDto, respDto);

                if (respDto.getMessage() != null) {
                    if (respDto.getMessage().contains("PL/SQL")) {
                        throw new MicApiException("Mua bảo hiểm MIC không thành công, vui lòng liên hệ nhân viên tư vấn để được hỗ trợ!");
                    } else {
                        throw new MicApiException(respDto.getMessage());
                    }
                }
                throw new MicApiException(MIC_MSG05);
            }

            if (Objects.isNull(respDto.getData()) || Objects.isNull(respDto.getData().getGcn())) { // không có gcn MIC
                log.error("[MIC]--Lỗi không tạo được GCNBH Sandbox MIC. Input {}. Response {}", reqDto, respDto);

                if (respDto.getMessage() != null) {
                    if (respDto.getMessage().contains("PL/SQL")) {
                        throw new MicApiException("Mua bảo hiểm MIC không thành công, vui lòng liên hệ nhân viên tư vấn để được hỗ trợ!");
                    } else {
                        throw new MicApiException(respDto.getMessage());
                    }
                }
                throw new MicApiException(MIC_MSG05);
            }

            return respDto;
        } catch (Exception e) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(e, MicSandboxError.class);
            throw new MicApi3rdException(errorObject.getErrorMessage(), errorObject.toErrorDto());
        }
    }

}
