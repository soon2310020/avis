package com.stg.entity;

import com.stg.entity.customer.Customer;
import com.stg.utils.Constants;
import com.stg.utils.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "illustration_table")
public class IllustrationTable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "illustration_number")
    private String illustrationNumber;

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

    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @Column(name = "package_code")
    private String packageCode;
    @Column(name = "death_no_accident_from")
    private String deathNoAccidentFrom;
    @Column(name = "str_death_no_accident_from")
    private String strDeathNoAccidentFrom;
    @Column(name = "death_no_accident_to")
    private String deathNoAccidentTo;
    @Column(name = "str_death_no_accident_to")
    private String strDeathNoAccidentTo;
    @Column(name = "death_accident")
    private String deathAccident;
    @Column(name = "str_death_accident")
    private String strDeathAccident;
    @Column(name = "death_accident_yes_traffic")
    private String deathAccidentYesTraffic;
    @Column(name = "death_accident_no_traffic")
    private String deathAccidentNoTraffic;
    @Column(name = "sup_inpatient_hospital_fee")
    private String supInpatientHospitalFee;
    @Column(name = "insurance_fee")
    private String insuranceFee;
    @Column(name = "base_insurance_fee")
    private String baseInsuranceFee;
    @Column(name = "topup_insurance_fee")
    private String topupInsuranceFee;
    @Column(name = "pay_frequency")
    private String payFrequency;
    @Column(name = "time_frequency")
    private String timeFrequency;
    @Column(name = "amount")
    private long amount;

    // Tổng phí bảo hiểm MBAL + MIC
    @Column(name = "mix_insurance_fee")
    private String mixInsuranceFee;

    @Column(name = "bs1")
    private String bs1;

    @Column(name = "bs2")
    private String bs2;

    @Column(name = "bs3")
    private String bs3;

    @Column(name = "bs4")
    private String bs4;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "id.illustrationTable")
    private List<PavTable> pavTables = new ArrayList<>();

    public void setCreationTime(String creationTime) {
        this.creationTime = DateUtil.localDateTimeToString(DateUtil.DATE_DMY_HMS, creationTime);
    }

    public void setIllustrationNumber() {
        this.illustrationNumber = UUID.randomUUID().toString();
    }

    @PrePersist
    public void prePersist() {
        if (creationTime == null) {
            creationTime = LocalDateTime.now();
        }
    }

}
