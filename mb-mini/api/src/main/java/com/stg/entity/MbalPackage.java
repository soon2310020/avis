package com.stg.entity;

import java.io.Serializable;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "mbal_package")
public class MbalPackage implements Serializable {

    private static final long serialVersionUID = 4921390605713558147L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "insurance_fee")
    private Long insuranceFee;
    @Column(name = "str_insurance_fee")
    private String strInsuranceFee;
    @Column(name = "insurance_fee_str")
    private String insuranceFeeStr;
    @Column(name = "package_code")
    private String packageCode;
    @Column(name = "product_name")
    private String productName;
    @Column(name = "product_code")
    private String productCode;
    @Column(name = "type")
    private String type;
    @Column(name = "death_benefit")
    private String deathBenefit;
    @Column(name = "str_death_benefit")
    private String strDeathBenefit;
    @Column(name = "logo")
    private String logo;
    @Column(name = "payfrq_cd")
    private String payfrqCd;

    @Column(name = "active")
    private Boolean active;

    @Transient
    private String category;
}
