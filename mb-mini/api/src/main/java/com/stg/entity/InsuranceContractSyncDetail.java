package com.stg.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "insurance_contract_sync_detail")
public class InsuranceContractSyncDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_contract_sync_id")
    private InsuranceContractSync insuranceContractSync;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_payment_id")
    private InsurancePayment insurancePayment;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "inquiry_date")
    private LocalDate inquiryDate;

    @Column(name = "policy_eff_date") // Ngay HD phat hanh
    private LocalDate policyEffDate;

    @Column(name = "payfreq_text")
    private String payfreqText;

    @Column(name = "periodic_prem")
    private String periodicPrem; //Phi BH chinh

    @Column(name = "fee_amt")
    private String feeAmt;

    @Column(name = "over_due_amt")
    private String oveDueAmt;

    @Column(name = "suspense_amt")
    private String suspenseAmt;

    @Column(name = "payment_amt")
    private String paymentAmt;

    @Column(name = "payment_min_amt")
    private String paymentMinAmt;

    @Column(name = "insur_duration")
    private String insurDuration;

    @Column(name = "insured_name")
    private String insuredName; // Tên người được bảo hiểm

    @Column(name = "premium_type")
    private String premiumType; //Loại phí: OVERDUE: Phí quá hạn, DUE: Phí tới hạn, NDUE: Kỳ phí sắp tới (Next Due)

    @Column(name = "due_from_date")
    private LocalDate dueFromDate;

    @Column(name = "due_to_date")
    private LocalDate dueToDate;

    @Column(name = "due_amount")
    private BigDecimal dueAmount; //số tiền phí trong due peimium

    @Column(name = "insured_bp")
    private String insuredBp; // BP number của người được bảo hiểm

    @Column(name = "insured_dob")
    private LocalDate insuredDob; // Ngày sinh người được bảo hiểm

    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    @Column(name = "max_top_up")
    private BigDecimal maxTopUp;

    @Column(name = "min_top_up")
    private BigDecimal minTopUp; //Phi dong them

    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @LastModifiedDate
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @PrePersist
    public void prePersist() {
        if (creationTime == null) {
            creationTime = LocalDateTime.now();
        }
        if (lastUpdated == null) {
            lastUpdated = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdated = LocalDateTime.now();
    }


}
