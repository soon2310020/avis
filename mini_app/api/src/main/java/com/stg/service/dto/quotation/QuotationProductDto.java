package com.stg.service.dto.quotation;

import com.stg.service.dto.mbal.ProductType;
import com.stg.service.dto.mbal.QuotationModelYearItem;
import com.stg.service.dto.quotation.validation.Less;
import com.stg.service3rd.mbal.constant.BenefitType;
import com.stg.service3rd.mbal.constant.PaymentPeriod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Less(field = "premiumTerm", dependField = "policyTerm", orEqual = true, message = "premium_term must be less than or equal to policy_term")
public class QuotationProductDto {

	@NotNull
	private ProductType type;

	@NotNull
	private Integer policyTerm;

	@NotNull
	private Integer premiumTerm;

	@NotNull
	private BigDecimal sumAssured;

	private BigDecimal sumAssuredOutput; /***/

	public QuotationProductDto(ProductType type, Integer policyTerm, Integer premiumTerm, BigDecimal sumAssured) {
		this.type = type;
		this.policyTerm = policyTerm;
		this.premiumTerm = premiumTerm;
		this.sumAssured = sumAssured;
	}

	// more output!
	private String code;
	private PaymentPeriod paymentPeriod;
	private BenefitType insuredBenefit;
	private String beneficiaryName;
	private List<QuotationModelYearItem> yearItems;
	private BigDecimal basePremium;
	private BigDecimal regBasePrem;
	private String assuredId;

	public ProductType getProductType() {
		return type;
	}

	public void setProductType(ProductType productType) {
		this.type = productType;
	}
}
