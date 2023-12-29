package com.stg.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "pav_import")
public class PavImport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "package_code")
    private String packageCode;

    @Column(name = "age")
    private Integer age;

    @Column(name = "gender")
    private String gender;

    // nhom nghe
    @Column(name = "group_occupation_class")
    private Integer groupOccupationClass;

    // nam hop dong
    @Column(name = "contract_year")
    private Integer contractYear;

    //Tuổi NĐBH
    @Column(name = "age_insured")
    private Integer ageInsured;

    //Lãi suất minh họa - Giá trị tài khoản
    @Column(name = "illustrated_account_value")
    private String illustratedAccountValue;

    //Lãi suất minh họa - Giá trị hoàn lại
    @Column(name = "illustrated_refund_value")
    private String illustratedRefundValue;

    //Lãi suất cam kết - GTTK
    @Column(name = "committed_account_value")
    private String committedAccountValue;

    //Lãi suất Cam kết - Giá trị hoàn lại
    @Column(name = "committed_refund_value")
    private String committedRefundValue;

}
