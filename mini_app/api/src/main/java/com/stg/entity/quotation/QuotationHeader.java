package com.stg.entity.quotation;

import com.stg.constant.quotation.QuotationState;
import com.stg.constant.quotation.QuotationType;
import com.stg.entity.AbstractAuditingEntity;
import com.stg.entity.potentialcustomer.PotentialCustomerDirect;
import com.stg.service.dto.mbal.DiscountCode;
import com.stg.service3rd.mbal.constant.BenefitType;
import com.stg.service3rd.mbal.constant.PaymentPeriod;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "quotation_header")
@Getter
@Setter
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class QuotationHeader extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long processId;
    @Column(nullable = false, updatable = false, columnDefinition = "UUID default gen_random_uuid()") /* insertable = false, */
    private UUID uuid;

    @NotNull
    private Long quotationId;

    @NotNull
    private Long submissionId;

    @NotNull
    @Enumerated(EnumType.ORDINAL)
    private QuotationType type;

    @NotNull
    @Enumerated(EnumType.ORDINAL)
    private BenefitType packageBenefitType;

    @NotNull
    private Integer packagePolicyTerm;

    @NotNull
    private Integer packagePremiumTerm;

    @NotNull
    @Enumerated(EnumType.ORDINAL)
    private PaymentPeriod packagePaymentPeriod;

    @NotNull
    private BigDecimal packageSumAssured;

    @NotNull
    private BigDecimal packagePeriodicPremium;

    @NotNull
    @Enumerated(EnumType.ORDINAL)
    private DiscountCode packageDiscountCode;

    private String searchName;

    private String searchPhoneNumber;

    @NotNull
    private Boolean raiderDeductFund;

    @NotNull
    private Boolean raw = Boolean.TRUE;

    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @JoinColumn(name = "customer_id")
    private QuotationCustomer customer;

    @OneToMany(mappedBy = "quotationHeader", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @OrderBy("id")
    private List<QuotationCustomer> assureds;

    @OneToMany(mappedBy = "quotationHeader", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @OrderBy("id")
    private List<QuotationAmount> amounts;

    @OneToMany(mappedBy = "quotationHeader", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @OrderBy("id")
    private List<QuotationDetail> details;

    @Enumerated(EnumType.STRING)
    private QuotationState state;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @JoinColumn(name = "sale_id")
    private QuotationSupporter sale;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @JoinColumn(name = "referrer_id")
    private QuotationSupporter referrer;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @JoinColumn(name = "supporter_id")
    private QuotationSupporter supporter;

    @Type(type = "jsonb")
    private String rawData;

    @Type(type = "jsonb")
    private String healths;

    @OneToOne(mappedBy = "quotationHeader", fetch = FetchType.LAZY)
    private PotentialCustomerDirect direct;

    @Type(type = "jsonb")
    private String micHealths;
}
