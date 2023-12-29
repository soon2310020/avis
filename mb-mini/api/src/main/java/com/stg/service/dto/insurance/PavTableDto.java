package com.stg.service.dto.insurance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PavTableDto {

    private Integer contractYear;

    private String illustrationNumber;

    private String packageCode;

    private Integer age;

    private String gender;

    private Integer ageInsured;

    private String illustratedAccountValue;

    private String illustratedRefundValue;

    private String committedAccountValue;

    private String committedRefundValue;

    private String insuranceFee;

    private String deathBenefit;

    private Long basePremium;

    private Long topupPremium;

    private Long withdrawal;

    private Long selectedRateBaseValue;

    private Long selectedRateTopupValue;

    private Long selectedRateAccountValue;

    private Long selectedRateSurenderValue;

    private Long committedRateBaseValue;

    private Long committedRateTopupValue;

    private Long committedRateAccountValue;

    private Long committedRateSurenderValue;

    private Long lowRateDeathBenefit;

    private Long lowRateLoyaltyBonus;

    private Long lowRateAccountValue;

    private Long lowRateSurenderValue;

    private Long highRateDeathBenefit;

    private Long highRateLoyaltyBonus;

    private Long highRateAccountValue;

    private Long highRateSurenderValue;
}
