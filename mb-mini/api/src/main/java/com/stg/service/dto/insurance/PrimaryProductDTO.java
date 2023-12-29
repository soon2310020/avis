package com.stg.service.dto.insurance;

import com.stg.utils.BenefitType;
import com.stg.utils.DiscountGroup;
import com.stg.utils.PaymentPeriod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PrimaryProductDTO {

    private Long id;

    private Long insuranceRequestId;

    private Long primaryInsuredId;

    private Integer policyTerm;

    private BenefitType insuredBenefit;

    private Integer premiumTerm;

    private PaymentPeriod paymentPeriod;

    private Long sumAssured;

    private Long periodicPremium;

    private DiscountGroup discountGroup;

    private Long baseInsuranceFee;

    private Long topupInsuranceFee;
}
