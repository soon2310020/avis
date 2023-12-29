package com.stg.entity.potentialcustomer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import lombok.*;

import com.stg.constant.Gender;
import com.stg.constant.IdentificationType;
import com.stg.entity.AbstractAuditingEntity;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

@Entity
@Table(name = "potential_customer")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class PotentialCustomer extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String fullName;

    @NotNull
    private LocalDate dob;

    @NotNull
    private Gender gender;

    private IdentificationType identificationType;

    private String identificationId;

    private String email;

    private String phoneNumber;
    
    private Integer occupationId;

    private BigDecimal inputAmount;

    @NotNull
    private Boolean raw;

    @Type(type = "jsonb")
    private String caredProduct;

    @Column(length = 100)
    private String note;
    
    @Column(length = 50)
    private String crmId;
    
    @NotNull
    private Boolean syncCrm;
    
}
