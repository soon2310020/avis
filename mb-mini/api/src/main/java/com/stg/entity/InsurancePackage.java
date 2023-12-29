package com.stg.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "insurance_package")
public class InsurancePackage implements Serializable {

    private static final long serialVersionUID = -3077524865738357110L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "package_name")
    private String packageName;

    @Column(name = "total_fee")
    private String totalFee;  //tổng phí bảo hiểm

    @Column(name = "total_benefit")
    private String totalBenefit;  //tổng quyền lợi

    @Column(name = "issuer")
    private String issuer; // đơn vị phát hành

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "mic_package_id")
    private MicPackage micPackage;

    @Column(name = "sub_one")
    private boolean subOne;
    @Column(name = "sub_two")
    private boolean subTwo;
    @Column(name = "sub_three")
    private boolean subThree;
    @Column(name = "sub_four")
    private boolean subFour;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "mbal_package_id")
    private MbalPackage mbalPackage;

    //thời gian đóng phí mic
    @Column(name = "mic_fee_payment_time")
    private String feePaymentTime;

    //định kỳ đóng phí mic
    @Column(name = "mic_periodic_fee_payment")
    private String micPeriodicFee;

    //thời gian đóng phí mbal
    @Column(name = "mbal_fee_payment_time")
    private String mbalFeePaymentTime;

    //định kỳ đóng phí mbal
    @Column(name = "mbal_periodic_fee_payment")
    private String mbalPeriodicFeePaymentTime;

    //Loại gói bảo hiểm: FREE_STYLE, FIX_COMBO
    @Column(name = "type")
    private String type;

    //Loại gói 5 năm (HEALTHY), 10 năm (HAPPY)
    @Column(name = "category")
    private String category;

}
