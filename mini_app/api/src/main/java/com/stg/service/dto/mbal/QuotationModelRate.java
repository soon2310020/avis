package com.stg.service.dto.mbal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class QuotationModelRate {

	private BigDecimal baseAccountValue;
	private BigDecimal topupAccountValue;
	private BigDecimal accountValue;
	private BigDecimal surenderValue;

}
