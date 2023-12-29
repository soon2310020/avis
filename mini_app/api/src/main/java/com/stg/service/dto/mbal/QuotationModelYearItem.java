package com.stg.service.dto.mbal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class QuotationModelYearItem {

	private Integer policyYear;
	private Integer insuredAge;
	private BigDecimal basePremium;
	private BigDecimal topupPremium;
	private BigDecimal withdrawal;
	private QuotationModelInterestRate accountByInterestRate;
	private QuotationModelBenefit benefitByInvestmentRate;

	private Integer yearPol;
	private BigDecimal periodicPremium;
}
