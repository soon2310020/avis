package com.stg.service.dto.mbal;

import com.stg.entity.quotation.QuotationCustomer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GenerateQuotationAssured extends GenerateQuotationCustomer {

	private AssuredType type;
	
	public GenerateQuotationAssured(QuotationCustomer customer) {
		super(customer);
	}
}
