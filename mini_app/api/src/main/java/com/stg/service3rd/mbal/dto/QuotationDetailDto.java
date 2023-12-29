package com.stg.service3rd.mbal.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class QuotationDetailDto {

	private Integer policyYear;

	private Integer insuredAge;

	private BigDecimal basePremium;

	private BigDecimal topupPremium;

	private BigDecimal withdrawal;

	private BigDecimal selectedRateBaseValue;

	private BigDecimal selectedRateTopupValue;

	private BigDecimal selectedRateAccountValue;

	private BigDecimal selectedRateSurenderValue;

	private BigDecimal committedRateBaseValue;

	private BigDecimal committedRateTopupValue;

	private BigDecimal committedRateAccountValue;

	private BigDecimal committedRateSurenderValue;

	private BigDecimal lowRateDeathBenefit;

	private BigDecimal lowRateLoyaltyBonus;

	private BigDecimal lowRateAccountValue;

	private BigDecimal lowRateSurenderValue;

	private BigDecimal highRateDeathBenefit;

	private BigDecimal highRateLoyaltyBonus;

	private BigDecimal highRateAccountValue;

	private BigDecimal highRateSurenderValue;

}
