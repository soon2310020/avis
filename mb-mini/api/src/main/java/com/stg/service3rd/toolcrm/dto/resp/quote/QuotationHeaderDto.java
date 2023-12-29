package com.stg.service3rd.toolcrm.dto.resp.quote;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stg.service.dto.external.requestFlexible.FlexibleSubmitMicQuestionRequest;
import com.stg.service.dto.external.responseFlexible.QuotationAmountRespDto;
import com.stg.service.dto.validator.Less;
import com.stg.service3rd.toolcrm.constant.DiscountCode;
import com.stg.service3rd.toolcrm.constant.QuotationState;
import com.stg.service3rd.toolcrm.constant.QuotationType;
import com.stg.service3rd.toolcrm.validation.PolicyTerm;
import com.stg.utils.BenefitType;
import com.stg.utils.FlexibleCommon;
import com.stg.utils.PaymentPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Less(field = "packagePremiumTerm", dependField = "packagePolicyTerm", orEqual = true, message = "package_premium_term must be less than or equal package_policy_term")
@PolicyTerm
public class QuotationHeaderDto {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("uuid")
    private UUID uuid; /*flex-e-app*/

    @Schema(name = "ProcessId of flexible")
    @JsonProperty("process_id")
    private Long processId; /*flex-e-app*/

    @NotNull
    @JsonProperty("type")
    private QuotationType type;

	@Enumerated(EnumType.STRING)
    @JsonProperty("state")
	private QuotationState state;

    @NotNull
    @JsonProperty("package_benefit_type")
    private BenefitType packageBenefitType;

    @NotNull
    @JsonProperty("package_policy_term")
    private Integer packagePolicyTerm;

    @NotNull
    @JsonProperty("package_premium_term")
    private Integer packagePremiumTerm;

    @NotNull
    @JsonProperty("package_payment_period")
    private PaymentPeriod packagePaymentPeriod;

    @NotNull
    @JsonProperty("package_sum_assured")
    private BigDecimal packageSumAssured;

    @NotNull
    @JsonProperty("package_periodic_premium")
    private BigDecimal packagePeriodicPremium;

    @NotNull
    @Valid
    @JsonProperty("customer")
    private QuotationAssuredDto customer;

    @JsonProperty("customer_is_assured")
    private Boolean customerIsAssured = false;

    //@NotNull /* when customerIsAssured = true */
    @Valid
    @JsonProperty("assured")
    private QuotationAssuredDto assured;

    @Valid
    @JsonProperty("additional_assureds")
    private List<QuotationAssuredDto> additionalAssureds;

    @Valid
    @JsonProperty("amount")
    private QuotationAmountRespDto amount;

    @NotNull
    @JsonProperty("discount_code")
    private DiscountCode discountCode;

    @NotNull
    @JsonProperty("raider_deduct_fund")
    private boolean raiderDeductFund;

    @Schema(description = "Người thụ hưởng")
    @Valid
    @JsonProperty("beneficiary")
    private FlexibleCommon.BeneficiaryOutput beneficiary; /*flex-e-app*/

    @Schema(name = "Thông tin IC")
    @JsonProperty("sale")
    private FlexibleCommon.Sale sale; /*flex-e-app*/

    @Schema(name = "Thông tin supporter")
    @JsonProperty("supporter")
    private FlexibleCommon.RefererInput supporter; /*flex-e-app*/

    @Valid
    @Schema(name = "Thông tin referrer")
    @JsonProperty("referrer")
    private FlexibleCommon.RefererInput referrer; /*flex-e-app*/

    @JsonProperty("healths")
    private List<FlexibleSubmitQuestionInput> healths;

    @JsonProperty("mic_healths")
    private List<FlexibleSubmitMicQuestionRequest> micHealths;
}
