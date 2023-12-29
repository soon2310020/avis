package com.stg.service3rd.mic;

import com.stg.errors.MicApiException;
import com.stg.service.dto.mic.MicGetTokenReqDto;
import com.stg.service.dto.mic.MicInsuranceResultReqDto;
import com.stg.service.dto.mic.MiniMicFeeReqDto;
import com.stg.service3rd.common.dto.error.IErrorObject;
import com.stg.service3rd.common.utils.ApiUtil;
import com.stg.service3rd.mbal.dto.*;
import com.stg.service3rd.mic.adapter.*;
import com.stg.service3rd.mic.dto.error.MicSandboxError;
import com.stg.service3rd.mic.dto.req.MicGenerateInsuranceCertReqDto;
import com.stg.service3rd.mic.dto.resp.MicGenerateInsuranceCertRespDto;
import com.stg.service3rd.mic.dto.resp.MicSandboxSearchCertRespDto;
import com.stg.service3rd.mic.exception.MicApi3rdException;
import com.stg.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

import static com.stg.constant.CommonMessageError.MIC_MSG01;
import static com.stg.constant.CommonMessageError.MIC_MSG02;
import static com.stg.constant.CommonMessageError.MIC_MSG05;
import static com.stg.constant.CommonMessageError.MSG12;
import static com.stg.constant.Gender.MALE;
import static com.stg.service3rd.mbal.constant.RelationshipPolicyHolderType.POLICY_HOLDER;
import static com.stg.service3rd.mbal.dto.FlexibleCommon.validateFxMicInsuranceAge;
import static com.stg.service3rd.mic.utils.MicUtil.*;
import static com.stg.utils.DateUtil.DATE_DMY;
import static com.stg.utils.DateUtil.DATE_YYYY_MM_DD;
import static com.stg.utils.DateUtil.convertFormat;
import static com.stg.utils.quotation.QuoteUtil.validateInsuranceAge;

@Slf4j
@Service
@RequiredArgsConstructor
public class MicApi3rdServiceImpl implements MicApi3rdService {
    private final MicApiCaller micApiCaller;
    private final MicProperties micProperties;

    private final MicSandboxApiCaller micSandboxApiCaller;
    private final MicSandboxProperties micSandboxProperties;

    /***/
    @Override
    public MicGenerateInsuranceCertRespDto generateInsuranceCert(MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh ttinkh, long micFee, String transactionId, int nhom, FlexibleCommon.GcnMicCareDkbs gcnMicCareDkbs) throws Exception {
        MicGenerateInsuranceCertReqDto certReqDto = new MicGenerateInsuranceCertReqDto();
        certReqDto.setTtoan((double) micFee);

        MicGenerateInsuranceCertReqDto.GcnMicCareTtinhd gcnMicCareTtinhd = new MicGenerateInsuranceCertReqDto.GcnMicCareTtinhd();
        gcnMicCareTtinhd.setNhom(convertGroupMic(nhom));
        gcnMicCareTtinhd.setNgay_hl(DateUtil.localDateTimeToString(DATE_DMY, LocalDateTime.now()));
        gcnMicCareTtinhd.setSo_hd_bm("");
        gcnMicCareTtinhd.setMa_sp(MIC_PRODUCT_CODE);
        gcnMicCareTtinhd.setGcn_miccare_dkbs(gcnMicCareDkbs);

        certReqDto.setGcn_miccare_ttin_hd(gcnMicCareTtinhd);
        certReqDto.setGcn_miccare_ttin_kh(ttinkh);

        MicGenerateInsuranceCertReqDto.GcnMicCareTtsk gcnMicCareTtsk = new MicGenerateInsuranceCertReqDto.GcnMicCareTtsk();
        gcnMicCareTtsk.setCk(NO);
        gcnMicCareTtsk.setCc(NO);
        gcnMicCareTtsk.setCu(NO);
        certReqDto.setGcn_miccare_ttsk(gcnMicCareTtsk);
        certReqDto.setNv(NV_CODE);
        certReqDto.setKieu_hd(CONTRACT_TYPE);
        certReqDto.setMa_dvi(micProperties.getDviCode());
        certReqDto.setNsd(micProperties.getNsd());
        certReqDto.setPas(micProperties.getPas());
        certReqDto.setId_tras(transactionId);
        certReqDto.setChecksum(micApiCaller.generateMicChecksum(transactionId));
        log.info("[MIC]--- Thông tin quyền lợi GCNBH MIC {}", gcnMicCareTtinhd);

        return micApiCaller.post(MicFunctions.GENERATE_INSURANCE_CERT, micApiCaller.getAuthHeader(), certReqDto, MicGenerateInsuranceCertRespDto.class);
    }

    /***/
    @Override
    public MicInsuranceResultRespDto flexibleMicFeeResult(MiniMicFeeReqDto miniMicFeeReqDto) throws Exception {
        validateFxMicInsuranceAge(miniMicFeeReqDto.getDob());
        MicInsuranceResultReqDto reqDto = new MicInsuranceResultReqDto();
        reqDto.setNv(NV_CODE);
        reqDto.setKieu_hd(CONTRACT_TYPE);
        reqDto.setMa_dvi(micProperties.getDviCode());
        reqDto.setNsd(micProperties.getNsd());
        reqDto.setPas(micProperties.getPas());
        reqDto.setId_tras("");
        reqDto.setNg_sinh(convertFormat(miniMicFeeReqDto.getDob(), DATE_YYYY_MM_DD, DATE_DMY));
        reqDto.setNgay_hl(DateUtil.localDateTimeToString(DATE_DMY, LocalDateTime.now()));
        reqDto.setNhom(convertGroupMic(miniMicFeeReqDto.getNhom()));
        reqDto.setMa_sp(MIC_PRODUCT_CODE);
        reqDto.setGcn_miccare_dkbs(miniMicFeeReqDto.getGcn_miccare_dkbs());
        reqDto.setChecksum(micApiCaller.generateMicChecksum(null));

        MicInsuranceResultRespDto respDto = micApiCaller.post(MicFunctions.MIC_INSURANCE_FEE_RESULT, micApiCaller.getAuthHeader(), reqDto, MicInsuranceResultRespDto.class);
        if (respDto != null && respDto.getCode().equals(MIC_CODE_02)) {
            throw new MicApiException(respDto.getMessage());
        }

        return respDto;
    }


    @Override
    public MicInsuranceResultRespDto flexibleMicFeeResult(FlexibleCommon.Assured assured, MicAdditionalProduct micRequest) throws Exception {
        validateInsuranceAge(assured.getDob());
        MicInsuranceResultReqDto reqDto = new MicInsuranceResultReqDto();
        reqDto.setNv(NV_CODE);
        reqDto.setKieu_hd(CONTRACT_TYPE);
        reqDto.setMa_dvi(micProperties.getDviCode());
        reqDto.setNsd(micProperties.getNsd());
        reqDto.setPas(micProperties.getPas());
        reqDto.setId_tras("");
        reqDto.setNg_sinh(convertFormat(assured.getDob(), DATE_YYYY_MM_DD, DATE_DMY));
        reqDto.setNgay_hl(DateUtil.localDateTimeToString(DATE_DMY, LocalDateTime.now()));
        reqDto.setNhom(convertGroupMic(micRequest.getNhom()));
        reqDto.setMa_sp(MIC_PRODUCT_CODE); /*add::maSp...*/ micRequest.setMaSp(MIC_PRODUCT_CODE);
        reqDto.setGcn_miccare_dkbs(new FlexibleCommon.GcnMicCareDkbs()
                .setBs1(micRequest.getBs1())
                .setBs2(micRequest.getBs2())
                .setBs3(micRequest.getBs3())
                .setBs4(micRequest.getBs4()));
        reqDto.setChecksum(micApiCaller.generateMicChecksum(null));

        MicInsuranceResultRespDto respDto = micApiCaller.post(MicFunctions.MIC_INSURANCE_FEE_RESULT, micApiCaller.getAuthHeader(), reqDto, MicInsuranceResultRespDto.class);
        if (respDto != null && respDto.getCode().equals(MIC_CODE_02)) {
            log.error("[MIC]--Call to mic error with detail {}", respDto.getMessage());
            throw new MicApiException(respDto.getMessage());
        }
        return respDto;
    }

    @Override
    public MicGetTokenRespDto micSandboxToken() {
        MicGetTokenReqDto reqDto = new MicGetTokenReqDto();
        reqDto.setUsername(micSandboxProperties.getUsername());
        reqDto.setPassword(micSandboxProperties.getPassword());
        reqDto.setClientId(micSandboxProperties.getClientId());
        reqDto.setClientSecret(micSandboxProperties.getClientSecret());
        try {
            MicGetTokenRespDto respDto = micSandboxApiCaller.post(MicFunctions.MIC_SANDBOX_GET_TOKEN, micApiCaller.getAuthHeader(), reqDto, MicGetTokenRespDto.class);
            if (respDto != null && respDto.getCode().equals(MIC_CODE_02)) {
                throw new MicApiException(respDto.getMessage());
            }
            return respDto;
        } catch (Exception e) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(e, MicSandboxError.class);
            throw new MicApi3rdException(errorObject.getErrorMessage(), errorObject.toErrorDto());
        }
    }

    @Override
    public MicSandboxFeeCareRespDto flexibleMicSandboxFee(FlexibleCommon.Assured assured, MicAdditionalProduct micRequest) {

        MicInsuranceResultReqDto reqDto = new MicInsuranceResultReqDto();
        reqDto.setNv(NV_CODE);
        reqDto.setKieu_hd(CONTRACT_TYPE);
        reqDto.setMa_sp(MIC_PRODUCT_CODE);
        reqDto.setNgay_hl(DateUtil.localDateTimeToString(DATE_DMY, LocalDateTime.now()));
        reqDto.setNg_sinh(convertFormat(assured.getDob(), DATE_YYYY_MM_DD, DATE_DMY));
        reqDto.setNhom(convertGroupMic(micRequest.getNhom()));
        FlexibleCommon.ParentContract parentContract = micRequest.getParentContract() == null ? new FlexibleCommon.ParentContract() : micRequest.getParentContract();
        reqDto.setTtin_hd_bme(parentContract);
        reqDto.setGcn_miccare_dkbs(new FlexibleCommon.GcnMicCareDkbs()
                .setBs1(micRequest.getBs1())
                .setBs2(micRequest.getBs2())
                .setBs3(micRequest.getBs3())
                .setBs4(micRequest.getBs4()));

        MicGetTokenRespDto micToken = micSandboxToken();
        String merchant = "";
        String token = "";
        if (micToken != null && micToken.getData() != null) {
            merchant = micToken.getData().getMerchant();
            token = micToken.getData().getAccess_token();
        }
        HttpHeaders authHeader = micSandboxApiCaller.getAuthHeader();
        authHeader.set(MERCHANT, merchant);
        authHeader.setBearerAuth(token);

        try {
            MicSandboxFeeCareRespDto respDto = micSandboxApiCaller.post(MicFunctions.MIC_SANDBOX_INSURANCE_FEE_RESULT, authHeader, reqDto, MicSandboxFeeCareRespDto.class);
            if (respDto != null && respDto.getCode().equals(MIC_CODE_02)) {
                throw new MicApiException(respDto.getMessage());
            }
            return respDto;
        } catch (Exception e) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(e, MicSandboxError.class);
            if (errorObject.getErrorMessage().contains(MIC_ERROR_CONTRACT)) {
                throw new MicApiException(String.format(MIC_MSG02, micRequest.getParentContract().getSo_hd_bm()));
            } else if (errorObject.getErrorMessage().contains(MIC_NOT_EXACTLY) || errorObject.getErrorMessage().contains(MIC_EXPIRED)) {
                throw new MicApiException(String.format(MIC_MSG01, micRequest.getParentContract().getSo_hd_bm()));
            }
            throw new MicApi3rdException(errorObject.getErrorMessage(), errorObject.toErrorDto());
        }
    }

    @Override
    public MicGenerateInsuranceCertSandboxRespDto micSandboxGenerateCert(MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh ttinkh,
                                                                         long micFee, String transactionId, int nhom,
                                                                         FlexibleCommon.GcnMicCareDkbs gcnMicCareDkbs,
                                                                         FlexibleCommon.ParentContract parentContract) {
        long startTime = System.currentTimeMillis();
        log.info("Start gen GCNBH MIC sandbox  voi transactionId {}", transactionId);
        MicGenerateInsuranceCertReqDto certReqDto = new MicGenerateInsuranceCertReqDto();
        certReqDto.setTtoan((double) micFee);

        MicGenerateInsuranceCertReqDto.GcnMicCareTtinhd gcnMicCareTtinhd = new MicGenerateInsuranceCertReqDto.GcnMicCareTtinhd();
        gcnMicCareTtinhd.setNhom(convertGroupMic(nhom));
        gcnMicCareTtinhd.setNgay_hl(DateUtil.localDateTimeToString(DATE_DMY, LocalDateTime.now()));
        gcnMicCareTtinhd.setNg_huong("");
        gcnMicCareTtinhd.setDongy(YES);
        gcnMicCareTtinhd.setGcn_miccare_dkbs(gcnMicCareDkbs);
        gcnMicCareTtinhd.setTtin_hd_bme(parentContract == null ? new FlexibleCommon.ParentContract() : parentContract);

        certReqDto.setGcn_miccare_ttin_hd(gcnMicCareTtinhd);

        certReqDto.setGcn_miccare_ttin_kh(ttinkh);

        MicGenerateInsuranceCertReqDto.GcnMicCareTtsk gcnMicCareTtsk = new MicGenerateInsuranceCertReqDto.GcnMicCareTtsk();
        gcnMicCareTtsk.setCk(NO);
        gcnMicCareTtsk.setCc(NO);
        gcnMicCareTtsk.setCu(NO);
        gcnMicCareTtsk.setTt72C(NO);
        gcnMicCareTtsk.setTt73C(NO);
        gcnMicCareTtsk.setTt74C(NO);
        gcnMicCareTtsk.setTt75C(NO);
        gcnMicCareTtsk.setTt76C(NO);
        gcnMicCareTtsk.setTt77C(NO);
        gcnMicCareTtsk.setTt78C(NO);

        certReqDto.setGcn_miccare_ttsk(gcnMicCareTtsk);

        certReqDto.setNv(NV_CODE);
        certReqDto.setMa_sp(MIC_PRODUCT_CODE);
        certReqDto.setKieu_hd(CONTRACT_TYPE);
        certReqDto.setSo_hd_g("");
        certReqDto.setId_tras(transactionId);

        MicGetTokenRespDto micToken = micSandboxToken();
        String merchant = "";
        String token = "";
        if (micToken != null && micToken.getData() != null) {
            merchant = micToken.getData().getMerchant();
            token = micToken.getData().getAccess_token();
        }

        HttpHeaders authHeader = micSandboxApiCaller.getAuthHeader();
        authHeader.set(MERCHANT, merchant);
        authHeader.setBearerAuth(token);
        try {
            MicGenerateInsuranceCertSandboxRespDto respDto = micSandboxApiCaller.post(MicFunctions.MIC_SANDBOX_INSURANCE_CERT, authHeader, certReqDto, MicGenerateInsuranceCertSandboxRespDto.class);
            log.info("Total time micGenerateInsuranceCert with transactionId {} is {}. Response data {}", transactionId, System.currentTimeMillis() - startTime, respDto);

            if (Objects.isNull(respDto)) {
                log.error("[MIC]--Lỗi không tạo được GCNBH Sandbox MIC. Input {}. Response {}", certReqDto, null);
                throw new MicApiException(MIC_MSG05);
            }

            if (MIC_CODE_02.equals(respDto.getCode())) { // tạo không thành công
                log.error("[MIC]--Lỗi khi GEN GCNBH Sandbox MIC. Input {}. Response {}", certReqDto, respDto);

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
                log.error("[MIC]--Lỗi không tạo được GCNBH Sandbox MIC. Input {}. Response {}", certReqDto, respDto);

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

    @Override
    public MicSandboxSearchCertRespDto micSandboxSearchCert(String transactionId) {
        return null;
    }

    @Override
    public MicSandboxFeeCareRespDto flexibleMicFeeSandboxResult(MiniMicFeeReqDto miniMicFeeReqDto) {
        MicInsuranceResultReqDto reqDto = new MicInsuranceResultReqDto();
        reqDto.setNv(NV_CODE);
        reqDto.setKieu_hd(CONTRACT_TYPE);
        reqDto.setMa_sp(MIC_PRODUCT_CODE);
        reqDto.setNgay_hl(DateUtil.localDateTimeToString(DATE_DMY, LocalDateTime.now()));
        reqDto.setNg_sinh(convertFormat(miniMicFeeReqDto.getDob(), DATE_YYYY_MM_DD, DATE_DMY));
        reqDto.setNhom(convertGroupMic(miniMicFeeReqDto.getNhom()));
        reqDto.setTtin_hd_bme(miniMicFeeReqDto.getParentContract() == null ? new FlexibleCommon.ParentContract() : miniMicFeeReqDto.getParentContract());
        reqDto.setGcn_miccare_dkbs(miniMicFeeReqDto.getGcn_miccare_dkbs());
        MicGetTokenRespDto micToken = micSandboxToken();
        String merchant = "";
        String token = "";
        if (micToken != null && micToken.getData() != null) {
            merchant = micToken.getData().getMerchant();
            token = micToken.getData().getAccess_token();
        }

        HttpHeaders authHeader = micSandboxApiCaller.getAuthHeader();
        authHeader.set(MERCHANT, merchant);
        authHeader.setBearerAuth(token);

        try {
            MicSandboxFeeCareRespDto respDto = micSandboxApiCaller.post(MicFunctions.MIC_SANDBOX_INSURANCE_FEE_RESULT, authHeader, reqDto, MicSandboxFeeCareRespDto.class);
            if (respDto != null && MIC_CODE_02.equals(respDto.getCode())) {
                throw new MicApiException(respDto.getMessage());
            }
            return respDto;
        } catch (Exception e) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(e, MicSandboxError.class);
            throw new MicApi3rdException(errorObject.getErrorMessage(), errorObject.toErrorDto());
        }
    }

    @Override
    public MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh generateMicSandboxTtinkh(FlexibleCommon.Assured customer, FlexibleCommon.Assured assured) {
        MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh ttinkh = new MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh();
        ttinkh.setLkh("C");
        ttinkh.setQhe(assured.getRelationshipWithPolicyHolder().getMicRelationship());
        ttinkh.setTen(assured.getFullName());
        ttinkh.setCmt(assured.getIdentificationNumber());
        ttinkh.setGioi(MALE.equals(assured.getGender()) ? "1" : "2");
        ttinkh.setNg_sinh(convertFormat(assured.getDob(), DATE_YYYY_MM_DD, DATE_DMY));
        ttinkh.setDchi(assured.getAddress().getLine1());
        ttinkh.setEmail(assured.getEmail());
        ttinkh.setMobi(assured.getPhoneNumber());

        ttinkh.setDbhm(POLICY_HOLDER.equals(assured.getRelationshipWithPolicyHolder()) ? "K" : "C");
        ttinkh.setLkhm("C");
        ttinkh.setTenm(customer.getFullName());
        ttinkh.setCmtm(customer.getIdentificationNumber());
        ttinkh.setMobim(customer.getPhoneNumber());
        ttinkh.setEmailm(customer.getEmail());
        ttinkh.setNg_sinhm(convertFormat(customer.getDob(), DATE_YYYY_MM_DD, DATE_DMY));
        ttinkh.setDchim(customer.getAddress().getLine1());

        return ttinkh;
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED, dontRollbackOn=MicApi3rdException.class)
    public MicSandboxContractInfoRespDto micSandboxSearchContractInfo(String contractNum, String activeDate) {

        MicGetTokenRespDto micToken = micSandboxToken();
        String merchant = "";
        String token = "";
        if (micToken != null && micToken.getData() != null) {
            merchant = micToken.getData().getMerchant();
            token = micToken.getData().getAccess_token();
        }
        HttpHeaders authHeader = micSandboxApiCaller.getAuthHeader();
        authHeader.set(MERCHANT, merchant);
        authHeader.setBearerAuth(token);

        try {
            MicSandboxContractInfoRespDto certRespDto = micSandboxApiCaller.get(MicFunctions.MIC_SEARCH_CONTRACT_SANDBOX, authHeader, MicSandboxContractInfoRespDto.class,
                    Map.of("so_hd", contractNum, "ngay_hl", DateUtil.localDateTimeToString(DateUtil.DATE_DMY, LocalDateTime.now())));

            if (certRespDto == null || MIC_CODE_02.equals(certRespDto.getCode())) {
                log.error("[MIC]--Lỗi khi lấy GCNBH MIC sandbox, message {}", certRespDto == null ? null : certRespDto.getMessage());
                if (certRespDto != null && certRespDto.getMessage().contains(MIC_ERROR_CONTRACT)) {
                    throw new MicApiException(String.format(MIC_MSG02, contractNum));
                } else if (certRespDto != null && (certRespDto.getMessage().contains(MIC_NOT_EXACTLY) || certRespDto.getMessage().contains(MIC_EXPIRED))) {
                    throw new MicApiException(String.format(MIC_MSG01, contractNum));
                }
                throw new MicApiException(certRespDto == null ? MSG12 : certRespDto.getMessage());
            }
            return certRespDto;
        } catch (Exception e) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(e, MicSandboxError.class);
            throw new MicApi3rdException(errorObject.getErrorMessage(), errorObject.toErrorDto());
        }
    }

}
