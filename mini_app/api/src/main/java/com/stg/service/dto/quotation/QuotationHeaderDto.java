package com.stg.service.dto.quotation;

import com.stg.constant.quotation.QuotationState;
import com.stg.constant.quotation.QuotationType;
import com.stg.service.dto.mbal.DiscountCode;
import com.stg.service.dto.quotation.validation.IPolicyTerm;
import com.stg.service.dto.quotation.validation.Less;
import com.stg.service.dto.quotation.validation.PolicyTerm;
import com.stg.service3rd.mbal.constant.BenefitType;
import com.stg.service3rd.mbal.constant.PaymentPeriod;
import com.stg.service3rd.mbal.dto.FlexibleCommon;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Less(field = "packagePremiumTerm", dependField = "packagePolicyTerm", orEqual = true, message = "package_premium_term must be less than or equal package_policy_term")
@PolicyTerm
public class QuotationHeaderDto implements IPolicyTerm {
	private Long id;
	private UUID uuid; /*flex-e-app*/

	@ApiModelProperty(name = "ProcessId of flexible")
	private Long processId; /*flex-e-app*/

	@NotNull
	private QuotationType type;

	@Enumerated(EnumType.STRING)
	private QuotationState state;

	@NotNull
	private BenefitType packageBenefitType;

	@NotNull
	private Integer packagePolicyTerm;

	@NotNull
	private Integer packagePremiumTerm;

	@NotNull
	private PaymentPeriod packagePaymentPeriod;

	@NotNull
	private BigDecimal packageSumAssured;

	@NotNull
	private BigDecimal packagePeriodicPremium;

	@NotNull
	@Valid
	private QuotationAssuredDto customer;

	private boolean customerIsAssured = false;

	//@NotNull /* when customerIsAssured = true */
	@Valid
	private QuotationAssuredDto assured;

	@Valid
	private List<QuotationAssuredDto> additionalAssureds;

	@Valid
	private QuotationAmountDto amount;

	@NotNull
	private DiscountCode discountCode;

	@NotNull
	private boolean raiderDeductFund;

	@ApiModelProperty(notes = "Người thụ hưởng")
	@Valid
	private FlexibleCommon.BeneficiaryOutput beneficiary; /*flex-e-app*/

	@ApiModelProperty(name = "Thông tin IC")
	private FlexibleCommon.Sale sale; /*flex-e-app*/

	@ApiModelProperty(name = "Thông tin supporter")
	private FlexibleCommon.ReferrerInput supporter; /*flex-e-app*/

	@Valid
	@ApiModelProperty(name = "Thông tin referrer")
	private FlexibleCommon.ReferrerInput referrer; /*flex-e-app*/

	private List<FlexibleSubmitQuestionInput> healths;

	private List<FlexibleSubmitMicQuestionRequest> micHealths;


	public QuotationAssuredDto getAssured() {
		if (assured != null) return assured;
		if (customerIsAssured) {
			return customer;
		}
		return null;
	}

	@Override
	public boolean validatePolicyTerm() {
		return packagePolicyTerm + assured.calculateInsuranceAge(assured.getDob(), LocalDateTime.now()) <= 100;
	}
}
