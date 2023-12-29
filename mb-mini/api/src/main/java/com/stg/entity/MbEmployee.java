package com.stg.entity;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "mb_employee")
public class MbEmployee {
    @Id
    @Column(name = "identity_card_number")
    private String identityCardNumber;

    @Column(name = "mb_id")
    private String mbId;

    @Column(name = "employee_code")

    private String employeeCode;

    @Column(name = "employee_name")
    private String employeeName;

    @Column(name = "managing_unit")
    private String managingUnit;

    @Column(name = "email")
    private String email;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = true;
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MbEmployee that = (MbEmployee) o;
        return Objects.equal(identityCardNumber, that.identityCardNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identityCardNumber);
    }

    /**
     * identityCardNumber, mbId, employeeCode
     */
    public boolean isUnique(MbEmployee o) {
        if (this == o) return true;
        if (o == null) return false;
        return Objects.equal(identityCardNumber, o.identityCardNumber) && Objects.equal(mbId, o.mbId); //&& Objects.equal(employeeCode, o.employeeCode);
    }
}
