package com.stg.service.dto.mbal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class QuotationModelInvestmentRate {

	private BigDecimal deathBenefit;
	private BigDecimal loyaltyBonus;
	private BigDecimal accountValue;
	private BigDecimal surenderValue;

}
