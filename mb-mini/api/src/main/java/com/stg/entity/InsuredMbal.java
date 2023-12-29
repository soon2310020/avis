package com.stg.entity;

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
@Table(name = "insured_mbal")
public class InsuredMbal implements Serializable {

    private static final long serialVersionUID = 2990707257882964962L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    // giấy tờ tùy thân
    @Column(name = "identification")
    private String identification;

    @Column(name = "id_card_type")
    private String idCardType;

    @Column(name = "address")
    private String address;

    @Column(name = "gender")
    private Integer gender;

    @Column(name = "birthday")
    private String birthday;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phone;

    @Column(name = "bp_number")
    private String bpNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_mbal_id")
    private ProductMbal product;

    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @Column(name = "id_issued_place")
    private String idIssuedPlace;

    @PrePersist
    public void prePersist() {
        if (creationTime == null) {
            creationTime = LocalDateTime.now();
        }
    }
}

