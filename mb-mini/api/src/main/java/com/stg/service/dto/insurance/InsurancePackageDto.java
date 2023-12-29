package com.stg.service.dto.insurance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class InsurancePackageDto {

    private Integer id;

    private String packageName;

    private String totalFee;

    private String totalBenefit;

    private String issuer;

    private MicPackageDto micPackage;

    private boolean subOne;
    private boolean subTwo;
    private boolean subThree;
    private boolean subFour;

    private MbalPackageDto mbalPackage;

    private String feePaymentTime;

    private String micPeriodicFee;

    private String mbalFeePaymentTime;

    private String mbalPeriodicFeePaymentTime;

    private String packagePhoto;

    private String category;

    private String type;
}
