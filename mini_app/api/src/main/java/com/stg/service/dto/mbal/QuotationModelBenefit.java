package com.stg.service.dto.mbal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class QuotationModelBenefit {

	private QuotationModelInvestmentRate lowRate;
	private QuotationModelInvestmentRate highRate;

}
