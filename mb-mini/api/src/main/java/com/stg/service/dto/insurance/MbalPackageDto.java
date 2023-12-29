package com.stg.service.dto.insurance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class MbalPackageDto {

    private String name;

    private Long insuranceFee;

    private String strInsuranceFee;

    private String insuranceFeeStr;

    private String packageCode;

    private String productName;

    private String productCode;

    private String type;

    private String deathBenefit;

    private String strDeathBenefit;

    private String logo;

    private String payfrqCd;
}
