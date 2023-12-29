package com.stg.service.dto.external.response;

import com.stg.service.dto.external.MicInsuranceBenefitDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class PackageTypeRespDtos {

    private List<PackageTypeData> packages;

    private List<MicInsuranceBenefitDto> micInsuranceBenefits;

}
