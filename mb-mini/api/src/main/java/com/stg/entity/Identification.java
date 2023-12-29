package com.stg.entity;

import com.stg.entity.customer.Customer;
import com.stg.utils.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "identification")
public class Identification implements Serializable {

    private static final long serialVersionUID = 2990707257882964962L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // giấy tờ tùy thân
    @Column(name = "identification")
    private String identification;

    @Column(name = "type")
    private String type;

    @Column(name = "issue_place")
    private String issuePlace;

    @Column(name = "issue_date")
    private LocalDateTime issueDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insured_mbal_id")
    private InsuredMbal insuredMbal;

    public void setIssueDate(String issueDate) {
        if (issueDate != null) {
            this.issueDate = DateUtil.localDateTimeToString(DateUtil.DATE_YMD_HMS_01, issueDate + " 00:00:00");
        }
    }

    public void setExpiryDate(String expiryDate) {
        if (expiryDate != null) {
            this.expiryDate = DateUtil.localDateTimeToString(DateUtil.DATE_YMD_HMS_01, expiryDate + " 00:00:00");
        }
    }

    @PrePersist
    public void prePersist() {
        if (creationTime == null) {
            creationTime = LocalDateTime.now();
        }
    }
}
