package com.stg.entity;

import com.stg.entity.customer.Customer;
import com.stg.utils.SourceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "insurance_contract_sync")
public class InsuranceContractSync implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mb_contract_id")
    private InsuranceContract insuranceContract;

    // So HDBH MBAL
    @Column(name = "mbal_policy_number", unique = true)
    private String mbalPolicyNumber;

    @Column(name = "policy_eff_date") // Ngay HD hieu luc
    private LocalDate policyEffDate;

    // So AF.xxx
    @Column(name = "mbal_app_no")
    private String mbalAppNo;

    @Column(name = "policy_status")
    private String policyStatus;

    @Column(name = "prd_name") //Ten goi bao hiem: Vững Tương lai
    private String prdName;

    @Column(name = "ph_name")
    private String phName;

    @Column(name = "pay_frequency") //Dinh ky dong phi
    private String payFrequency;

    @Column(name = "currency")
    private String currency;

    @Column(name = "periodic_prem") //Phi BH chinh
    private String periodicPrem;

    @Column(name = "policy_status_id")
    private String policyStatusId;

    @Column(name = "agent_code")
    private String agentCode;

    @Column(name = "agent_name")
    private String agentName;

    @Column(name = "agent_num")
    private String agentNum;

    @Enumerated(EnumType.STRING)
    @Column(name = "source")
    private SourceType source;

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
