package com.stg.service.dto.mbal;

import com.stg.entity.quotation.QuotationProduct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GenerateQuotationAdditionalProduct {
	
	private ProductType productType;
	private Integer assuredIndex;
	private Integer policyTerm;
	private Integer premiumTerm;
	private BigDecimal sumAssured;
	
	public GenerateQuotationAdditionalProduct(Integer index, QuotationProduct product) {
		setAssuredIndex(index);
		setProductType(product.getType());
		setPolicyTerm(product.getPolicyTerm());
		setPremiumTerm(product.getPremiumTerm());
		setSumAssured(product.getSumAssured());
	}
}
