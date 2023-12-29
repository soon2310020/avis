package com.stg.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import com.stg.entity.customer.Customer;
import com.stg.utils.Constants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "insurance_contract")
public class InsuranceContract implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "package_type")
    private Constants.PackageType packageType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_package_id")
    private InsurancePackage insurancePackage;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mbal_package_id")
    private MbalPackage mbalPackage;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mic_package_id")
    private MicPackage micPackage;

    @Column(name = "mic_issue_date")
    private LocalDateTime micIssueDate;

    @Column(name = "mbal_issue_date")
    private LocalDateTime mbalIssueDate;

    // tong phi = total amount
    @Column(name = "str_insurance_fee")
    private String strInsuranceFee;

    // GCNBH MIC
    @Column(name = "mic_contract_num")
    private String micContractNum;

    // So HDBH MBAL
    @Column(name = "mbal_policy_number")
    private String mbalPolicyNumber;

    //thời gian đóng phí mic
    @Column(name = "mic_fee_payment_time")
    private String micFeePaymentTime;

    //định kỳ đóng phí mic
    @Column(name = "mic_periodic_fee_payment")
    private String micPeriodicFee;

    //thời gian đóng phí mbal
    @Column(name = "mbal_fee_payment_time")
    private String mbalFeePaymentTime;

    //định kỳ đóng phí mbal
    @Column(name = "mbal_periodic_fee_payment")
    private String mbalPeriodicFeePaymentTime;

    @Column(name = "logo")
    private String logo;

    // phi bao hiem MIC
    @Column(name = "mic_amount")
    private String micAmount;

    // phi bao hiem định kỳ MBAL
    @Column(name = "mbal_amount")
    private String mbalAmount;

    // GCNBH MBAL
    @Column(name = "mbal_app_no")
    private String mbalAppNo;

    @Column(name = "transaction_id", nullable = false, unique = true)
    private String transactionId;

    @Column(name = "mbal_product_name")
    private String mbalProductName;

    @Column(name = "mb_reference_number")
    private String mbReferenceNumber;

    private String source;

    private String status;

    @Column(name = "coverage_year")
    private Integer coverageYear; //Thời hạn bảo hiểm

    @Column(name = "start_date")
    private String startDate; //Ngày bắt đầu hiệu lực

    @Column(name = "end_date")
    private String endDate; //Ngày hết hạn

    @Column(name = "sum_insured")
    private Long sumInsured; //Số tiền bảo hiểm

    @PrePersist
    public void prePersist() {
        if (micIssueDate == null) {
            micIssueDate = LocalDateTime.now();
        }
    }

}
