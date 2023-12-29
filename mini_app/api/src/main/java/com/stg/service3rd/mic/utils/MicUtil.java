package com.stg.service3rd.mic.utils;

import com.stg.constant.Gender;
import com.stg.constant.MicCompareType;
import com.stg.errors.MicApiException;
import com.stg.service.dto.mic.MicCompareResp;
import com.stg.service.dto.mic.MicPackageReqDto;
import com.stg.service.dto.mic.MiniMicFeeReqDto;
import com.stg.service3rd.mbal.dto.FlexibleCommon;
import com.stg.service3rd.mbal.dto.MicAdditionalProduct;
import com.stg.service3rd.mbal.dto.MicSandboxContractInfoRespDto;
import com.stg.service3rd.mic.dto.req.MicGenerateInsuranceCertReqDto;
import com.stg.utils.DateUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.stg.constant.CommonMessageError.MIC_MSG03;
import static com.stg.constant.CommonMessageError.MIC_MSG04;
import static com.stg.constant.Gender.MALE;
import static com.stg.constant.MicGroup.getMicGroup;
import static com.stg.service3rd.mbal.dto.FlexibleCommon.ZERO_TIME;
import static com.stg.service3rd.mbal.dto.FlexibleCommon.insuranceAge;
import static com.stg.utils.DateUtil.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MicUtil {
    public static final String NO = "K";
    public static final String YES = "C";
    public static final String NV_CODE = "NG_SKC";
    public static final String CONTRACT_TYPE = "G";
    public static final String MIC_PRODUCT_CODE = "MICCARE_MBAL_2";
    public static final String MIC_CODE_02 = "02";
    public static final String MIC_NO = "Không";
    public static final String MERCHANT = "merchant";

    public static final String MIC_EXPIRED = "khong phai hop dong MICCARE hoac het hieu luc";
    public static final String MIC_NOT_EXACTLY = "Hợp đồng không chính xác";
    public static final String MIC_ERROR_CONTRACT = "Nguoi duoc bao hiem thuoc hop dong";

    /***/
    public static String convertGroupMic(int nhom) {
        switch (nhom) {
            case 1:
                return "1/MBAL2";
            case 2:
                return "2/MBAL2";
            case 3:
                return "3/MBAL2";
            case 4:
                return "4/MBAL2";
            default:
                return "5/MBAL2";
        }
    }

    /***/
    public static MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh generateMicCareTtinkh(String fullname, String idCardNo,
                                                                                        String gender, String dob,
                                                                                        String address, String email, String phone) {
        MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh ttinkh = new MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh();
        ttinkh.setLkh(YES);
        ttinkh.setTen(fullname);
        ttinkh.setCmt(idCardNo);
        ttinkh.setGioi(gender.equals("MALE") ? "1" : "2");
        ttinkh.setNg_sinh(convertFormat(dob, DATE_YYYY_MM_DD, DATE_DMY));
        ttinkh.setDchi(address);
        ttinkh.setEmail(email);
        ttinkh.setMobi(phone);
        return ttinkh;
    }

    /***/
    public static MiniMicFeeReqDto genMicFeeReqDto(String dob, int nhom) {
        MiniMicFeeReqDto micFeeReqDto = new MiniMicFeeReqDto();
        micFeeReqDto.setNhom(nhom);
        micFeeReqDto.setDob(dob);
        FlexibleCommon.GcnMicCareDkbs gcnMicCareDkbs = new FlexibleCommon.GcnMicCareDkbs();
        gcnMicCareDkbs.setBs1(NO);
        gcnMicCareDkbs.setBs2(NO);
        gcnMicCareDkbs.setBs3(NO);
        gcnMicCareDkbs.setBs4(NO);
        micFeeReqDto.setGcn_miccare_dkbs(gcnMicCareDkbs);
        micFeeReqDto.setGcn_miccare_dkbs(gcnMicCareDkbs);

        return micFeeReqDto;
    }

    public static boolean checkMicParentRequest(MicSandboxContractInfoRespDto.ContractInfo micContractInfo,
                                                MicAdditionalProduct policyHolderRequest) {

        if (getMicGroup(micContractInfo.getNhom()) >= policyHolderRequest.getNhom()) {
            // check dkbs
            if (micContractInfo.getBs1().equals(NO) && policyHolderRequest.getBs1().equals(YES)) {
                return false;
            }
            if (micContractInfo.getBs2().equals(NO) && policyHolderRequest.getBs2().equals(YES)) {
                return false;
            }
            if (micContractInfo.getBs3().equals(NO) && policyHolderRequest.getBs3().equals(YES)) {
                return false;
            }
            return !micContractInfo.getBs4().equals(NO) || !policyHolderRequest.getBs4().equals(YES);
        }
        return false;
    }

//    public static boolean checkMicParentRequestWithChildrenRequest(MicAdditionalProduct micChildrenRequest,
//                                                                   MicAdditionalProduct policyHolderRequest) {
//
//        if (policyHolderRequest.getNhom() >= micChildrenRequest.getNhom()) {
//            // check dkbs
//            if (policyHolderRequest.getBs1().equals(NO) && micChildrenRequest.getBs1().equals(YES)) {
//                return false;
//            }
//            if (policyHolderRequest.getBs2().equals(NO) && micChildrenRequest.getBs2().equals(YES)) {
//                return false;
//            }
//            if (policyHolderRequest.getBs3().equals(NO) && micChildrenRequest.getBs3().equals(YES)) {
//                return false;
//            }
//            return !policyHolderRequest.getBs4().equals(NO) || !micChildrenRequest.getBs4().equals(YES);
//        }
//        return false;
//    }

    public static void validateMicRequest(String dob, MicAdditionalProduct micRequest, Gender gender) {
        if (Objects.nonNull(micRequest) && MALE.equals(gender) && YES.equals(micRequest.getBs3())) {
            throw new MicApiException(MIC_MSG04);
        }
        int age = insuranceAge(DateUtil.localDateTimeToString(DATE_YMD_HMS_01, dob + ZERO_TIME), LocalDateTime.now());
        if ((age < 18 || age > 50) && Objects.nonNull(micRequest) &&
                (micRequest.getNhom() == 3 || micRequest.getNhom() == 4 || micRequest.getNhom() == 5) && YES.equals(micRequest.getBs3())) {
            throw new MicApiException(MIC_MSG03);
        }
    }


    private static boolean checkMicParentRequestWithChildrenRequest(MicAdditionalProduct micChildrenRequest,
                                                                    MicAdditionalProduct micCompareRequest) {

        if (Objects.nonNull(micCompareRequest) && micCompareRequest.getNhom() >= micChildrenRequest.getNhom()) {
            // check dkbs
            if (micCompareRequest.getBs1().equals(NO) && micChildrenRequest.getBs1().equals(YES)) {
                return false;
            }
            if (micCompareRequest.getBs2().equals(NO) && micChildrenRequest.getBs2().equals(YES)) {
                return false;
            }
            if (micCompareRequest.getBs3().equals(NO) && micChildrenRequest.getBs3().equals(YES)) {
                return false;
            }
            return !micCompareRequest.getBs4().equals(NO) || !micChildrenRequest.getBs4().equals(YES);
        }
        return false;
    }

    public static MicCompareResp checkMicParentRequestWithChildrenRequest(MicAdditionalProduct micChildrenRequest,
                                                                           MicAdditionalProduct policyHolderRequest,
                                                                           FlexibleCommon.Assured customer,
                                                                           MicAdditionalProduct coupleRequest,
                                                                           FlexibleCommon.Assured coupleAssured,
                                                                           MicSandboxContractInfoRespDto.ContractInfo contractInfo,
                                                                           FlexibleCommon.ParentContract parentContract) {
        MicCompareResp compareResp = new MicCompareResp();
        micChildrenRequest.setParentContract(new FlexibleCommon.ParentContract()
                .setSo_hd_bm("")
                .setCmt_bm("")
                .setMobi_bm("")
                .setTen_bm(""));
        boolean childrenWithHolder = checkMicParentRequestWithChildrenRequest(micChildrenRequest, policyHolderRequest);
        if (childrenWithHolder) {
            micChildrenRequest.setParentContract(new FlexibleCommon.ParentContract()
                    .setSo_hd_bm("")
                    .setCmt_bm(customer.getIdentificationNumber())
                    .setMobi_bm(customer.getPhoneNumber())
                    .setTen_bm(customer.getFullName()));
            compareResp.setResult(true);
            compareResp.setFromContractInput(false);
            compareResp.setCompareType(MicCompareType.POLICY_HOLDER);
            return compareResp;
        }
        boolean withCouple = checkMicParentRequestWithChildrenRequest(micChildrenRequest, coupleRequest);
        if (withCouple) {
            micChildrenRequest.setParentContract(new FlexibleCommon.ParentContract()
                    .setSo_hd_bm("")
                    .setCmt_bm(coupleAssured.getIdentificationNumber())
                    .setMobi_bm(coupleAssured.getPhoneNumber())
                    .setTen_bm(coupleAssured.getFullName()));
            compareResp.setResult(true);
            compareResp.setFromContractInput(false);
            compareResp.setCompareType(MicCompareType.COUPLE_INSURED);
            return compareResp;
        }
        boolean withParentContract = checkMicParentRequestWithChildrenRequest(micChildrenRequest, contractInfo == null ? null : new MicAdditionalProduct(contractInfo));
        if (withParentContract && Objects.nonNull(contractInfo)) {
            micChildrenRequest.setParentContract(new FlexibleCommon.ParentContract()
                    .setSo_hd_bm(parentContract.getSo_hd_bm())
                    .setCmt_bm(contractInfo.getCmt())
                    .setMobi_bm(contractInfo.getMobi())
                    .setTen_bm(contractInfo.getTen()));
            compareResp.setResult(true);
            compareResp.setFromContractInput(true);
            compareResp.setCompareType(MicCompareType.PARENT_CONTRACT);
            return compareResp;
        }
        return compareResp;
    }

    public static MicCompareResp checkGroupMic(MicAdditionalProduct micParentRequest,
                                               MicAdditionalProduct micContractInput) {
        MicCompareResp compareResp = new MicCompareResp();
        if (Objects.nonNull(micParentRequest) && Objects.nonNull(micContractInput)) {
            int max = Math.max(micContractInput.getNhom(), micParentRequest.getNhom());
            compareResp.setNhom(max);
            compareResp.setCompareType(micContractInput.getNhom() >= micParentRequest.getNhom() ? MicCompareType.PARENT_CONTRACT : MicCompareType.POLICY_HOLDER);
            return compareResp;
        } else if (Objects.nonNull(micParentRequest)) {
            compareResp.setNhom(micParentRequest.getNhom());
            compareResp.setCompareType(MicCompareType.POLICY_HOLDER);
            return compareResp;
        } else if (Objects.nonNull(micContractInput)) {
            compareResp.setNhom(micContractInput.getNhom());
            compareResp.setCompareType(MicCompareType.PARENT_CONTRACT);
            return compareResp;
        }
        return compareResp;
    }

    public static void genParentContract(MicCompareResp compareResp, MiniMicFeeReqDto micFeeReq, MicPackageReqDto reqDto) {
        if (compareResp.getNhom() == null) {
            micFeeReq.setParentContract(new FlexibleCommon.ParentContract());
            return;
        }
        switch (compareResp.getCompareType()) {
            case POLICY_HOLDER:
                micFeeReq.setParentContract(new FlexibleCommon.ParentContract().setSo_hd_bm("")
                        .setTen_bm(reqDto.getParentInfo().getFullName())
                        .setMobi_bm(reqDto.getParentInfo().getPhoneNumber())
                        .setCmt_bm(reqDto.getParentInfo().getIdentificationNumber()));
                break;
            case PARENT_CONTRACT:
                micFeeReq.setParentContract(new FlexibleCommon.ParentContract()
                        .setSo_hd_bm(reqDto.getParentContract().getSo_hd_bm())
                        .setTen_bm(reqDto.getParentContract().getTen_bm())
                        .setMobi_bm(reqDto.getParentContract().getMobi_bm())
                        .setCmt_bm(reqDto.getParentContract().getCmt_bm()));
                break;
            default:
                micFeeReq.setParentContract(new FlexibleCommon.ParentContract());
        }
    }


    public static String replaceWithAsterisks(String input, int start, int end) {
        if (input == null || input.isEmpty() || start >= end || start < 0 ) {
            return input;
        }
        StringBuilder result = new StringBuilder(input);
        for (int i = start; i <= end; i++) {
            if (i > input.length() - 1) {
                break;
            }
            result.setCharAt(i, '*');
        }
        return result.toString();
    }
}
