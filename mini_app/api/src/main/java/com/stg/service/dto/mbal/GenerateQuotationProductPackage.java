package com.stg.service.dto.mbal;

import com.stg.entity.quotation.QuotationHeader;
import com.stg.service3rd.mbal.constant.BenefitType;
import com.stg.service3rd.mbal.constant.PaymentPeriod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GenerateQuotationProductPackage {

	private BenefitType benefitType;
	private Integer policyTerm;
	private Integer premiumTerm;
	private PaymentPeriod paymentPeriod;
	private BigDecimal sumAssured;
	private BigDecimal periodicPremium;
	private DiscountCode discountCode;
	
	public GenerateQuotationProductPackage(QuotationHeader header) {
		setBenefitType(header.getPackageBenefitType());
		setPolicyTerm(header.getPackagePolicyTerm());
		setPremiumTerm(header.getPackagePremiumTerm());
		setPaymentPeriod(header.getPackagePaymentPeriod());
		setSumAssured(header.getPackageSumAssured());
		setPeriodicPremium(header.getPackagePeriodicPremium());
		setDiscountCode(header.getPackageDiscountCode());
	}

}
