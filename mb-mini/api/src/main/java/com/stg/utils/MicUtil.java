package com.stg.utils;

import com.stg.service.dto.external.request.MicPackageReqDto;
import com.stg.service.dto.external.request.MiniMicFeeReqDto;
import com.stg.service.dto.external.requestFlexible.MicAdditionalProduct;
import com.stg.service.dto.external.response.MicCompareResp;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;


@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MicUtil {

    public static final String MIC_EXPIRED = "khong phai hop dong MICCARE hoac het hieu luc";
    public static final String MIC_NOT_EXACTLY = "Hợp đồng không chính xác";
    public static final String MIC_ERROR_CONTRACT = "Nguoi duoc bao hiem thuoc hop dong";
    public static final String MIC_CONTRACT_CANCEL = "da huy";

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
            micFeeReq.setParentContract(new Common.ParentContract());
            return;
        }
        switch (compareResp.getCompareType()) {
            case POLICY_HOLDER:
                micFeeReq.setParentContract(new Common.ParentContract().setSo_hd_bm("")
                        .setTen_bm(reqDto.getParentInfo().getFullName())
                        .setMobi_bm(reqDto.getParentInfo().getPhoneNumber())
                        .setCmt_bm(reqDto.getParentInfo().getIdentificationNumber()));
                break;
            case PARENT_CONTRACT:
                micFeeReq.setParentContract(new Common.ParentContract()
                        .setSo_hd_bm(reqDto.getParentContract().getSo_hd_bm())
                        .setTen_bm(reqDto.getParentContract().getTen_bm())
                        .setMobi_bm(reqDto.getParentContract().getMobi_bm())
                        .setCmt_bm(reqDto.getParentContract().getCmt_bm()));
                break;
            default:
                micFeeReq.setParentContract(new Common.ParentContract());
        }
    }
}
