package com.stg.service.dto.mbal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class QuotationModelInterestRate {

	private QuotationModelRate selectedRate;
	private QuotationModelRate committedRate;

}
