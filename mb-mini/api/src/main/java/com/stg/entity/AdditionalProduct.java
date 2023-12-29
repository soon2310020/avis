package com.stg.entity;

import com.stg.entity.customer.Customer;
import com.stg.utils.BenefitType;
import com.stg.utils.Constants;
import com.stg.utils.PaymentPeriod;
import com.stg.utils.ProductType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "additional_product")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class AdditionalProduct implements Serializable {

    private static final long serialVersionUID = 2990707257882964962L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_request_id")
    private InsuranceRequest insuranceRequest;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_insured_id")
    private PrimaryInsured primaryInsured;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "additional_insured_id")
    private AdditionalInsured additionalInsured;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mic_package_id")
    private MicPackage micPackage;

    @Column(name = "bs1")
    private String bs1;

    @Column(name = "bs2")
    private String bs2;

    @Column(name = "bs3")
    private String bs3;

    @Column(name = "bs4")
    private String bs4;

    //sản phẩm bổ trợ COI_RIDER , ADDR, CIR ,..
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mbal_package_id")
    private MbalPackage mbalPackage;

    //thời hạn hợp đồng
    @Column(name = "contract_period")
    private String contractPeriod;

    //thời gian đóng phí
    @Column(name = "fee_payment_time")
    private String feePaymentTime;

    //số tiền bảo hiểm
    @Column(name = "amount")
    private String amount;

    //đồng ý khấu trừ phí bảo hiểm khi đến hạn thanh toán
    @Column(name = "agree_deduct_premiums_due")
    private String agreeDeductPremiumsDue;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private Constants.PackageType type; // MBAL hoặc MIC

    private String code;

    @Column(name = "policy_term")
    private Integer policyTerm;

    @Column(name = "premium_term")
    private Integer premiumTerm;

    @Column(name = "sum_assured")
    private Long sumAssured;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_period")
    private PaymentPeriod paymentPeriod;

    @Enumerated(EnumType.STRING)
    @Column(name = "insured_benefit")
    private BenefitType insuredBenefit;

    @Column(name = "beneficiary_name")
    private String beneficiaryName;

    @Column(name = "base_premium")
    private Long basePremium;

    @Column(name = "reg_base_prem")
    private Long regBasePrem;

    @Column(name = "assured_id")
    private String assuredId;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type")
    private ProductType productType;

    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @Column(name = "mic_fee")
    private BigDecimal micFee;

    @Column(name = "mic_sum_benefit")
    private BigDecimal micSumBenefit ;

    @Column(name = "mic_transaction_id")
    private String micTransactionId;

    // GCNBH MIC
    @Column(name = "mic_contract_num")
    private String micContractNum;

    @Column(name = "parent_mic_contract")
    @Type(type = "jsonb")
    private String parentMicContract;

    @PrePersist
    public void prePersist() {
        if (creationTime == null) {
            creationTime = LocalDateTime.now();
        }
    }
}
