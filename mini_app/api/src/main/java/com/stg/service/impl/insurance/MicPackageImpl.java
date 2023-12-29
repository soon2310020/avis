package com.stg.service.impl.insurance;

import com.stg.entity.insurance.MicPackage;
import com.stg.errors.MicApiException;
import com.stg.repository.MicPackageRepository;
import com.stg.service.MicPackageService;
import com.stg.service.dto.mic.MicCompareResp;
import com.stg.service.dto.mic.MicInsuranceBenefitV2Dto;
import com.stg.service.dto.mic.MicPackageReqDto;
import com.stg.service.dto.mic.MiniMicFeeReqDto;
import com.stg.service3rd.common.dto.error.IErrorObject;
import com.stg.service3rd.common.utils.ApiUtil;
import com.stg.service3rd.mbal.constant.Common;
import com.stg.service3rd.mbal.dto.*;
import com.stg.service3rd.mbal.dto.error.MbalErrorObject;
import com.stg.service3rd.mic.MicApi3rdService;
import com.stg.service3rd.mic.exception.MicApi3rdException;
import com.stg.service3rd.mic.utils.MicUtil;
import com.stg.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.stg.constant.Gender.MALE;
import static com.stg.service3rd.mbal.dto.FlexibleCommon.ZERO_TIME;
import static com.stg.service3rd.mbal.dto.FlexibleCommon.insuranceAge;
import static com.stg.service3rd.mic.utils.MicUtil.*;
import static com.stg.utils.DateUtil.DATE_YMD_HMS_01;
import static com.stg.utils.quotation.QuoteUtil.validateAdditionalFxMicInsuranceAge;

@Slf4j
@Service
@RequiredArgsConstructor
public class MicPackageImpl implements MicPackageService {
    private final ModelMapper modelMapper;

    private final MicPackageRepository micPackageRepository;
    private final MicApi3rdService micApi3rdService;

    @Override
    @Cacheable(value = "cache:micPackages", unless = "#result.size()==0")
    public List<MicPackage> list() {
        return micPackageRepository.findAllByOrderByIdAsc();
    }

    @Override
    public List<MicInsuranceBenefitV2Dto> retrieveMicPackages(MicPackageReqDto reqDto) {
        try {
            validateAdditionalFxMicInsuranceAge(reqDto.getDob());

            List<MicPackage> micPackages = this.list(); // micPackageRepository.findAll()
            List<MicInsuranceBenefitV2Dto> micInsuranceBenefits = micPackages.stream()
                    .map(micPackage -> modelMapper.map(micPackage, MicInsuranceBenefitV2Dto.class))
                    .collect(Collectors.toList());
            List<MicInsuranceBenefitV2Dto> micBenefits = new ArrayList<>();

            //BRONZE
            Optional<MicInsuranceBenefitV2Dto> insuranceBenefitId1 = micInsuranceBenefits.stream().filter(o -> o.getId() == 1).findFirst();
            if (insuranceBenefitId1.isPresent()) {
                MicInsuranceBenefitV2Dto micBronzer = insuranceBenefitId1.get();
                MiniMicFeeReqDto bronzerMicFeeReq = genMicFeeReqDto(reqDto.getDob(), 1);
                MicInsuranceResultRespDto bronzeFee = micApi3rdService.flexibleMicFeeResult(bronzerMicFeeReq);
                micBronzer.setPhi(bronzeFee.getPhi());

                micBenefits.add(micBronzer);
            }

            //SILVER
            Optional<MicInsuranceBenefitV2Dto> micInsuranceBenefitId2 = micInsuranceBenefits.stream().filter(o -> o.getId() == 2).findFirst();
            if (micInsuranceBenefitId2.isPresent()) {
                MicInsuranceBenefitV2Dto micSilver = micInsuranceBenefitId2.get();
                MiniMicFeeReqDto silverMicFeeReq = genMicFeeReqDto(reqDto.getDob(), 2);
                MicInsuranceResultRespDto silverFee = micApi3rdService.flexibleMicFeeResult(silverMicFeeReq);
                micSilver.setPhi(silverFee.getPhi());

                micBenefits.add(micSilver);
            }

            //GOLD
            Optional<MicInsuranceBenefitV2Dto> micInsuranceBenefitId3 = micInsuranceBenefits.stream().filter(o -> o.getId() == 3).findFirst();
            if (micInsuranceBenefitId3.isPresent()) {
                MicInsuranceBenefitV2Dto micGold = micInsuranceBenefitId3.get();
                MiniMicFeeReqDto goldMicFeeReq = genMicFeeReqDto(reqDto.getDob(), 3);
                MicInsuranceResultRespDto goldFee = micApi3rdService.flexibleMicFeeResult(goldMicFeeReq);
                micGold.setPhi(goldFee.getPhi());

                micBenefits.add(micGold);
            }

            //PLATINUM
            Optional<MicInsuranceBenefitV2Dto> micInsuranceBenefitId4 = micInsuranceBenefits.stream().filter(o -> o.getId() == 4).findFirst();
            if (micInsuranceBenefitId4.isPresent()) {
                MicInsuranceBenefitV2Dto micPlatinum = micInsuranceBenefitId4.get();
                MiniMicFeeReqDto platinumMicFeeReq = genMicFeeReqDto(reqDto.getDob(), 4);
                MicInsuranceResultRespDto platinumFee = micApi3rdService.flexibleMicFeeResult(platinumMicFeeReq);
                micPlatinum.setPhi(platinumFee.getPhi());

                micBenefits.add(micPlatinum);
            }

            //PLATINUM
            Optional<MicInsuranceBenefitV2Dto> micInsuranceBenefitId5 = micInsuranceBenefits.stream().filter(o -> o.getId() == 5).findFirst();
            if (micInsuranceBenefitId5.isPresent()) {
                MicInsuranceBenefitV2Dto micDiamond = micInsuranceBenefitId5.get();
                MiniMicFeeReqDto diamondMicFeeReq = genMicFeeReqDto(reqDto.getDob(), 5);
                MicInsuranceResultRespDto diamondFee = micApi3rdService.flexibleMicFeeResult(diamondMicFeeReq);
                micDiamond.setPhi(diamondFee.getPhi());
                micBenefits.add(micDiamond);
            }

            return micBenefits;
        } catch (Exception e) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(e, MbalErrorObject.class);
            throw new MicApi3rdException(errorObject.getErrorMessage(), errorObject.toErrorDto());
        }
    }

    @Override
    public List<MicInsuranceBenefitV2Dto> retrieveMicSandboxPackages(MicPackageReqDto reqDto) {
        int ages = insuranceAge(DateUtil.localDateTimeToString(DATE_YMD_HMS_01, reqDto.getDob() + ZERO_TIME), LocalDateTime.now());

        List<MicPackage> micPackages = micPackageRepository.findAll();
        List<MicInsuranceBenefitV2Dto> micInsuranceBenefits = micPackages.stream()
                .map(micPackage -> modelMapper.map(micPackage, MicInsuranceBenefitV2Dto.class))
                .collect(Collectors.toList());
        List<MicInsuranceBenefitV2Dto> micBenefits = new ArrayList<>();
        FlexibleCommon.ParentContract parentContract = reqDto.getParentContract();
        MicSandboxContractInfoRespDto parentContractInfo;
        MicCompareResp groupMic;

        if (parentContract != null) {
            try {
                parentContractInfo = micSandboxSearchContractInfo(parentContract.getSo_hd_bm(),
                        DateUtil.localDateTimeToString(DateUtil.DATE_DMY, LocalDateTime.now()), true);
                groupMic = MicUtil.checkGroupMic(reqDto.getParentInfo() == null ? null : reqDto.getParentInfo().getMicRequest(), new MicAdditionalProduct(parentContractInfo.getData()));
            } catch (MicApiException e) {
                log.error("[MIC]--HD bố/mẹ không đủ điều kiện apply {}", parentContract.getSo_hd_bm());
                groupMic = MicUtil.checkGroupMic(reqDto.getParentInfo() == null ? null : reqDto.getParentInfo().getMicRequest(), null);
            }
        } else {
            groupMic = MicUtil.checkGroupMic(reqDto.getParentInfo() == null ? null : reqDto.getParentInfo().getMicRequest(), null);
        }

        //BRONZE
        Optional<MicInsuranceBenefitV2Dto> insuranceBenefitId1 = micInsuranceBenefits.stream().filter(o -> o.getId() == 1).findFirst();
        if (insuranceBenefitId1.isPresent()) {
            MicInsuranceBenefitV2Dto micBronzer = insuranceBenefitId1.get();
            if (MALE.equals(reqDto.getGender())) {
                micBronzer.setSubThree(MIC_NO);
            }
            MiniMicFeeReqDto bronzerMicFeeReq = genMicFeeReqDto(reqDto.getDob(), 1);
            bronzerMicFeeReq.setParentContract(reqDto.getParentContract());
            if (Objects.nonNull(groupMic.getNhom())) {
                genParentContract(groupMic, bronzerMicFeeReq, reqDto);
            }
            MicSandboxFeeCareRespDto bronzeFee = flexibleMicFeeSandboxResult(bronzerMicFeeReq);
            micBronzer.setPhi(bronzeFee.getData().getPhi());
            addNoDisCountFee(micBronzer, ages, bronzerMicFeeReq);

            micBenefits.add(micBronzer);
        }

        //SILVER
        Optional<MicInsuranceBenefitV2Dto> micInsuranceBenefitId2 = micInsuranceBenefits.stream().filter(o -> o.getId() == 2).findFirst();
        if (micInsuranceBenefitId2.isPresent()) {
            MicInsuranceBenefitV2Dto micSilver = micInsuranceBenefitId2.get();
            if (MALE.equals(reqDto.getGender())) {
                micSilver.setSubThree(MIC_NO);
            }
            MiniMicFeeReqDto silverMicFeeReq = genMicFeeReqDto(reqDto.getDob(), 2);
            silverMicFeeReq.setParentContract(reqDto.getParentContract());
            if (Objects.nonNull(groupMic.getNhom()) &&groupMic.getNhom() >= 2) {
                genParentContract(groupMic, silverMicFeeReq, reqDto);
            }
            MicSandboxFeeCareRespDto silverFee = flexibleMicFeeSandboxResult(silverMicFeeReq);
            micSilver.setPhi(silverFee.getData().getPhi());

            if (Objects.nonNull(groupMic.getNhom()) &&groupMic.getNhom() >= 2) {
                addNoDisCountFee(micSilver, ages, silverMicFeeReq);
            }
            micBenefits.add(micSilver);
        }

        //GOLD
        Optional<MicInsuranceBenefitV2Dto> micInsuranceBenefitId3 = micInsuranceBenefits.stream().filter(o -> o.getId() == 3).findFirst();
        if (micInsuranceBenefitId3.isPresent()) {
            MicInsuranceBenefitV2Dto micGold = micInsuranceBenefitId3.get();
            if (ages < 18 || ages > 50 || MALE.equals(reqDto.getGender())) {
                micGold.setSubThree(MIC_NO);
            }
            MiniMicFeeReqDto goldMicFeeReq = genMicFeeReqDto(reqDto.getDob(), 3);
            goldMicFeeReq.setParentContract(reqDto.getParentContract());
            if (Objects.nonNull(groupMic.getNhom()) && groupMic.getNhom() >= 3) {
                genParentContract(groupMic, goldMicFeeReq, reqDto);
            }
            MicSandboxFeeCareRespDto goldFee = flexibleMicFeeSandboxResult(goldMicFeeReq);
            micGold.setPhi(goldFee.getData().getPhi());

            if (Objects.nonNull(groupMic.getNhom()) && groupMic.getNhom() >= 3) {
                addNoDisCountFee(micGold, ages, goldMicFeeReq);
            }
            micBenefits.add(micGold);
        }

        //PLATINUM
        Optional<MicInsuranceBenefitV2Dto> micInsuranceBenefitId4 = micInsuranceBenefits.stream().filter(o -> o.getId() == 4).findFirst();
        if (micInsuranceBenefitId4.isPresent()) {
            MicInsuranceBenefitV2Dto micPlatinum = micInsuranceBenefitId4.get();
            if (ages < 18 || ages > 50 || MALE.equals(reqDto.getGender())) {
                micPlatinum.setSubThree(MIC_NO);
            }
            MiniMicFeeReqDto platinumMicFeeReq = genMicFeeReqDto(reqDto.getDob(), 4);
            platinumMicFeeReq.setParentContract(reqDto.getParentContract());
            if (Objects.nonNull(groupMic.getNhom()) && groupMic.getNhom() >= 4) {
                genParentContract(groupMic, platinumMicFeeReq, reqDto);
            }
            MicSandboxFeeCareRespDto platinumFee = flexibleMicFeeSandboxResult(platinumMicFeeReq);
            micPlatinum.setPhi(platinumFee.getData().getPhi());

            if (Objects.nonNull(groupMic.getNhom()) && groupMic.getNhom() >= 4) {
                addNoDisCountFee(micPlatinum, ages, platinumMicFeeReq);
            }
            micBenefits.add(micPlatinum);
        }

        //PLATINUM
        Optional<MicInsuranceBenefitV2Dto> micInsuranceBenefitId5 = micInsuranceBenefits.stream().filter(o -> o.getId() == 5).findFirst();
        if (micInsuranceBenefitId5.isPresent()) {
            MicInsuranceBenefitV2Dto micDiamond = micInsuranceBenefitId5.get();
            if (ages < 18 || ages > 50 || MALE.equals(reqDto.getGender())) {
                micDiamond.setSubThree(MIC_NO);
            }
            MiniMicFeeReqDto diamondMicFeeReq = genMicFeeReqDto(reqDto.getDob(), 5);
            diamondMicFeeReq.setParentContract(reqDto.getParentContract());
            if (Objects.nonNull(groupMic.getNhom()) && groupMic.getNhom() == 5) {
                genParentContract(groupMic, diamondMicFeeReq, reqDto);
            }
            MicSandboxFeeCareRespDto diamondFee = flexibleMicFeeSandboxResult(diamondMicFeeReq);
            micDiamond.setPhi(diamondFee.getData().getPhi());
            micBenefits.add(micDiamond);

            if (Objects.nonNull(groupMic.getNhom()) && groupMic.getNhom() == 5) {
                addNoDisCountFee(micDiamond, ages, diamondMicFeeReq);
            }
        }

        return micBenefits;
    }

    @Override
    public MicSandboxFeeCareRespDto flexibleMicFeeSandboxResult(MiniMicFeeReqDto miniMicFeeReqDto) {
        return micApi3rdService.flexibleMicFeeSandboxResult(miniMicFeeReqDto);
    }

    @Override
    public MicSandboxContractInfoRespDto micSandboxSearchContractInfo(String contractNum, String activeDate, boolean show) {
        MicSandboxContractInfoRespDto contractInfo = micApi3rdService.micSandboxSearchContractInfo(contractNum, activeDate);
        if (!show && contractInfo != null && contractInfo.getData() != null) {
            contractInfo.getData().setCmt(replaceWithAsterisks(contractInfo.getData().getCmt(), 2, 5));
            contractInfo.getData().setTen(replaceWithAsterisks(contractInfo.getData().getTen(), 3, 6));
            contractInfo.getData().setMobi(replaceWithAsterisks(contractInfo.getData().getMobi(), 3, 6));
            return contractInfo;
        }
        return contractInfo;
    }

    private void addNoDisCountFee(MicInsuranceBenefitV2Dto micBenefit, int ages, MiniMicFeeReqDto micFeeReq) {
        if (ages < 6) {
            micFeeReq.setParentContract(new FlexibleCommon.ParentContract());
            MicSandboxFeeCareRespDto bronzeNoDiscountFee = flexibleMicFeeSandboxResult(micFeeReq);
            micBenefit.setRegularFee(bronzeNoDiscountFee.getData().getPhi());
        }
    }

}
