package com.stg.entity.customer;

import com.stg.entity.InsuranceContract;
import com.stg.entity.Segment;
import com.stg.utils.DateUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import lombok.*;
import org.springframework.util.StringUtils;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "customer")
public class Customer implements Serializable {

    private static final long serialVersionUID = 2990707257882964962L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mb_id")
    private String mbId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "full_name_t24")
    private String fullNameT24;

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
    private String address; /*origin address from T24*/

    @Column(name = "line1")
    private String line1;
    @Column(name = "province_name")
    private String provinceName;
    @Column(name = "district_name")
    private String districtName;
    @Column(name = "ward_name")
    private String wardName;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "customer")
    private List<InsuranceContract> insuranceContracts; // used by JPQL

    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "segment_id")
    private Segment segment;

    @Column(name = "identification_date")
    private LocalDateTime identificationDate;

    @Column(name = "id_issued_place")
    private String idIssuedPlace;

    @LastModifiedDate
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    private String source;

    @Column(name = "managing_unit")
    private String managingUnit; // Đơn vị quản lý thuộc tập đoàn MB

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

    public String fullAddress() {
        String addressDto = this.getLine1() != null ? this.getLine1() : this.getAddress();
        if (StringUtils.hasText(this.getWardName())) addressDto += " - " + this.getWardName();
        if (StringUtils.hasText(this.getDistrictName())) addressDto += " - " + this.getDistrictName();
        if (StringUtils.hasText(this.getProvinceName())) addressDto += " - " + this.getProvinceName();
        return addressDto;
    }

    public String fullNameOrDefaultT24() {
        if (this.getFullName() == null) return this.getFullNameT24();
        return this.getFullName();
    }

    public <T> T accept(CustomerVisitor<T> v) {
        return v.visit(this);
    }

    public void setBirthday(String birthday) {
        if (birthday != null) {
            this.birthday = DateUtil.localDateTimeToString(DateUtil.DATE_YMD_HMS_01, birthday + " 00:00:00");
        }
    }

    public void setIdentificationDate(String identificationDate) {
        if (identificationDate != null) {
            this.identificationDate = DateUtil.localDateTimeToString(DateUtil.DATE_YMD_HMS_01, identificationDate + " 00:00:00");
        }
    }

    public boolean isMBGroup() {
        return StringUtils.hasText(managingUnit);
    }
}
