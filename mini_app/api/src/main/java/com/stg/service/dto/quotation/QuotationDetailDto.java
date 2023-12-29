package com.stg.service.dto.quotation;

import com.stg.entity.quotation.QuotationDetail;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class QuotationDetailDto {

	private Integer policyYear;

	private Integer insuredAge;

	private BigDecimal basePremium;

	private BigDecimal topupPremium;

	private BigDecimal withdrawal;

	private BigDecimal selectedRateBaseValue;

	private BigDecimal selectedRateTopupValue;

	private BigDecimal selectedRateAccountValue;

	private BigDecimal selectedRateSurenderValue;

	private BigDecimal committedRateBaseValue;

	private BigDecimal committedRateTopupValue;

	private BigDecimal committedRateAccountValue;

	private BigDecimal committedRateSurenderValue;

	private BigDecimal lowRateDeathBenefit;

	private BigDecimal lowRateLoyaltyBonus;

	private BigDecimal lowRateAccountValue;

	private BigDecimal lowRateSurenderValue;

	private BigDecimal highRateDeathBenefit;

	private BigDecimal highRateLoyaltyBonus;

	private BigDecimal highRateAccountValue;

	private BigDecimal highRateSurenderValue;
	
	public QuotationDetailDto(QuotationDetail detail) {
		setPolicyYear(detail.getPolicyYear());
		setInsuredAge(detail.getInsuredAge());
		setBasePremium(detail.getBasePremium());
		setTopupPremium(detail.getTopupPremium());
		setWithdrawal(detail.getWithdrawal());
		setSelectedRateBaseValue(detail.getSelectedRateBaseValue());
		setSelectedRateTopupValue(detail.getSelectedRateTopupValue());
		setSelectedRateAccountValue(detail.getSelectedRateAccountValue());
		setSelectedRateSurenderValue(detail.getSelectedRateSurenderValue());
		setCommittedRateBaseValue(detail.getCommittedRateBaseValue());
		setCommittedRateTopupValue(detail.getCommittedRateTopupValue());
		setCommittedRateAccountValue(detail.getCommittedRateAccountValue());
		setCommittedRateSurenderValue(detail.getCommittedRateSurenderValue());
		setLowRateDeathBenefit(detail.getLowRateDeathBenefit());
		setLowRateLoyaltyBonus(detail.getLowRateLoyaltyBonus());
		setLowRateAccountValue(detail.getLowRateAccountValue());
		setLowRateSurenderValue(detail.getLowRateSurenderValue());
		setHighRateDeathBenefit(detail.getHighRateDeathBenefit());
		setHighRateLoyaltyBonus(detail.getHighRateLoyaltyBonus());
		setHighRateAccountValue(detail.getHighRateAccountValue());
		setHighRateSurenderValue(detail.getHighRateSurenderValue());
	}

}
