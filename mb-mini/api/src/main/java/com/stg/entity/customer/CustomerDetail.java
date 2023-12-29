package com.stg.entity.customer;

import com.stg.entity.InsuranceRequest;
import com.stg.entity.Segment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "customer_detail")
public class CustomerDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mb_id")
    private String mbId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "gender")
    private String gender;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "segment_id")
    private Segment segment;

    @Column(name = "identification_date")
    private LocalDateTime identificationDate;

    @Column(name = "id_issued_place")
    private String idIssuedPlace;

    @Column(name = "annual_income")
    private BigDecimal annualIncome;

}
