package com.stg.entity;

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
@Table(name = "beneficiary") // Người thụ hưởng
public class Beneficiary implements Serializable {

    private static final long serialVersionUID = 2990707257882964962L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "gender")
    private String gender;

    @Column(name = "email")
    private String email;

    @Column(name = "birthday")
    private LocalDateTime birthday;

    @Column(name = "phone_number")
    private String phone;

    // giấy tờ tùy thân
    @Column(name = "identification")
    private String identification;

    @Column(name = "id_card_type")
    private String idCardType;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "job")
    private String job;

    @Column(name = "address")
    private String address;

    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_request_id")
    private InsuranceRequest insuranceRequest;

    @Column(name = "insured_id")
    private String insuredId; // giá trị của mbal

    public void setBirthday(String birthday) {
        if (birthday != null) {
            this.birthday = DateUtil.localDateTimeToString(DateUtil.DATE_YMD_HMS_01, birthday + " 00:00:00");
        }
    }

    @PrePersist
    public void prePersist() {
        if (creationTime == null) {
            creationTime = LocalDateTime.now();
        }
    }
}
