package com.stg.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "pav_table")
public class PavTable implements Serializable {

    @EmbeddedId
    private PavTableId id;

    @Column(name = "package_code")
    private String packageCode;
    @Column(name = "age")
    private Integer age;
    @Column(name = "gender")
    private String gender;

    //Tuổi NĐBH
    @Column(name = "age_insured")
    private Integer ageInsured;

    //Lãi suất minh họa - Giá trị tài khoản
    @Column(name = "illustrated_account_value")
    private String illustratedAccountValue;

    //Lãi suất minh họa - Giá trị hoàn lại
    @Column(name = "illustrated_refundvalue")
    private String illustratedRefundValue;

    //Lãi suất cam kết - GTTK
    @Column(name = "committedaccount_value")
    private String committedAccountValue;

    //Lãi suất Cam kết - Giá trị hoàn lại
    @Column(name = "committed_refund_value")
    private String committedRefundValue;

    // Phí bảo hiểm cơ bản NT
    @Column(name = "insurance_fee")
    private String insuranceFee;

    // Quyền lợi tử vong/TTTBVV
    @Column(name = "death_benefit")
    private String deathBenefit;

    @Column(name = "base_premium")
    private Long basePremium;

    @Column(name = "topup_premium")
    private Long topupPremium;

    @Column(name = "withdrawal")
    private Long withdrawal;

    @Column(name = "selected_rate_base_value")
    private Long selectedRateBaseValue;

    @Column(name = "selected_rate_topup_value")
    private Long selectedRateTopupValue;

    @Column(name = "selected_rate_account_value")
    private Long selectedRateAccountValue;

    @Column(name = "selected_rate_surender_value")
    private Long selectedRateSurenderValue;

    @Column(name = "committed_rat0e_base_value")
    private Long committedRateBaseValue;

    @Column(name = "committed_rate_topup_value")
    private Long committedRateTopupValue;

    @Column(name = "committed_rate_account_value")
    private Long committedRateAccountValue;

    @Column(name = "committed_rate_surender_value")
    private Long committedRateSurenderValue;

    @Column(name = "low_rate_death_benefit")
    private Long lowRateDeathBenefit;

    @Column(name = "low_rate_loyalty_bonus")
    private Long lowRateLoyaltyBonus;

    @Column(name = "low_rate_account_value")
    private Long lowRateAccountValue;

    @Column(name = "low_rate_surender_value")
    private Long lowRateSurenderValue;

    @Column(name = "high_rate_death_benefit")
    private Long highRateDeathBenefit;

    @Column(name = "high_rate_loyalty_bonus")
    private Long highRateLoyaltyBonus;

    @Column(name = "high_rate_account_value")
    private Long highRateAccountValue;

    @Column(name = "high_rate_surender_value")
    private Long highRateSurenderValue;


    @Embeddable
    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PavTableId implements Serializable {
        @ManyToOne
        @EqualsAndHashCode.Exclude @ToString.Exclude
        @JoinColumn(name="illustration_number", nullable = false)
        private IllustrationTable illustrationTable;

        //Năm hợp đồng
        @Column(name = "number_of_contract_year")
        private Integer contractYear;
    }
}
