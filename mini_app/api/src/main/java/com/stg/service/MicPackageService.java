package com.stg.service;

import com.stg.entity.insurance.MicPackage;
import com.stg.service.dto.mic.MicInsuranceBenefitV2Dto;
import com.stg.service.dto.mic.MicPackageReqDto;
import com.stg.service.dto.mic.MiniMicFeeReqDto;
import com.stg.service3rd.mbal.dto.MicSandboxContractInfoRespDto;
import com.stg.service3rd.mbal.dto.MicSandboxFeeCareRespDto;

import java.util.List;

public interface MicPackageService {

    List<MicPackage> list();

    List<MicInsuranceBenefitV2Dto> retrieveMicPackages(MicPackageReqDto reqDto);

    List<MicInsuranceBenefitV2Dto> retrieveMicSandboxPackages(MicPackageReqDto reqDto);

    MicSandboxFeeCareRespDto flexibleMicFeeSandboxResult(MiniMicFeeReqDto miniMicFeeReqDto);

    MicSandboxContractInfoRespDto micSandboxSearchContractInfo(String contractNum, String activeDate, boolean show);
}
