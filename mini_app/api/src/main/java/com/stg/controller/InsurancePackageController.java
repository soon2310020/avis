package com.stg.controller;

import com.stg.entity.insurance.MicPackage;
import com.stg.service.MicPackageService;
import com.stg.service.dto.mic.MicInsuranceBenefitV2Dto;
import com.stg.service.dto.mic.MicPackageReqDto;
import com.stg.service3rd.mbal.dto.MicSandboxContractInfoRespDto;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.stg.common.Endpoints.INSURANCE_PACKAGE;

@Validated
@RequiredArgsConstructor
@RestController
@Api(tags = "Insurance Package Apis")
public class InsurancePackageController {
    private final MicPackageService micPackageService;

//    @GetMapping(INSURANCE_PACKAGE.MIC_PACKAGE)
//    @ResponseStatus(HttpStatus.OK)
//    public List<MicPackage> micPackages() {
//        return micPackageService.list();
//    }

    @PostMapping(INSURANCE_PACKAGE.MIC_PACKAGE)
    @ResponseStatus(HttpStatus.OK)
    public List<MicInsuranceBenefitV2Dto> retrieveMicPackages(@Valid @RequestBody MicPackageReqDto reqDto) {
        return micPackageService.retrieveMicSandboxPackages(reqDto);
    }

    @GetMapping(INSURANCE_PACKAGE.MIC_CONTRACT)
    @ResponseStatus(HttpStatus.OK)
    public MicSandboxContractInfoRespDto micSandboxSearchContractInfo(@RequestParam(value = "contractNum") String contractNum,
                                                                      @RequestParam(value = "activeDate", required = false, defaultValue = "22/08/2022") String activeDate) {
        return micPackageService.micSandboxSearchContractInfo(contractNum, activeDate, false);
    }
}
