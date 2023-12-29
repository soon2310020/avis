package com.stg.service.dto.mbal;

import com.stg.entity.quotation.QuotationAmount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GenerateQuotationAmount {
	
	private AmountType type;
	private BigDecimal value;
	private Integer startYear;
	private Integer endYear;
	
	public GenerateQuotationAmount(QuotationAmount amount) {
		setType(amount.getType());
		setValue(amount.getValue());
		setStartYear(amount.getStartYear());
		setEndYear(amount.getEndYear());
	}
}
