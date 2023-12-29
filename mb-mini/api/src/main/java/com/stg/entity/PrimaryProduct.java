package com.stg.entity;

import com.stg.utils.BenefitType;
import com.stg.utils.DiscountGroup;
import com.stg.utils.PaymentPeriod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "primary_product")
public class PrimaryProduct implements Serializable {

    private static final long serialVersionUID = 2990707257882964962L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_request_id")
    private InsuranceRequest insuranceRequest;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_insured_id")
    private PrimaryInsured primaryInsured;

    @Column(name = "policy_term")
    private Integer policyTerm;

    @Enumerated(EnumType.STRING)
    @Column(name = "insured_benefit")
    private BenefitType insuredBenefit;

    @Column(name = "premium_term")
    private Integer premiumTerm;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_period")
    private PaymentPeriod paymentPeriod;

    @Column(name = "sum_assured")
    private Long sumAssured;

    @Column(name = "periodic_premium")
    private Long periodicPremium;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_group")
    private DiscountGroup discountGroup;

    @Column(name = "base_insurance_fee")
    private Long baseInsuranceFee;

    @Column(name = "topup_insurance_fee")
    private Long topupInsuranceFee;

    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @PrePersist
    public void prePersist() {
        if (creationTime == null) {
            creationTime = LocalDateTime.now();
        }
    }
}
