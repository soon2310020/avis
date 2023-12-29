package com.stg.service.dto.external.responseV2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class PackageTypeV2RespDtos {

    private List<PackageTypeV2Data> packages;

    private List<MicInsuranceBenefitV2Dto> micInsuranceBenefits;

}
