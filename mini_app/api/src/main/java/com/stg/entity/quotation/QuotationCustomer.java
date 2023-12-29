package com.stg.entity.quotation;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.util.StringUtils;

import com.stg.constant.Gender;
import com.stg.constant.IdentificationType;
import com.stg.entity.AbstractAuditingEntity;
import com.stg.entity.mic.MicInsuranceContract;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "quotation_customer")
public class QuotationCustomer extends AbstractAuditingEntity implements Serializable {
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

    @NotNull
    private Integer occupationId;

    @NotNull
    private Boolean married;

    @NotNull
    private IdentificationType identificationType;

    @NotNull
    private String identificationId;

    private String phoneNumber;

    private String email;

    @ManyToOne
    @JoinColumn(name = "quotation_header_id")
    private QuotationHeader quotationHeader;

    @OneToMany(mappedBy = "quotationCustomer", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @OrderBy("id")
    private List<QuotationProduct> additionalProducts; /*MBAL*/

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @JoinColumn(name = "mic_insurance_contract_id")
    private MicInsuranceContract micInsuranceContract;  /*MIC*/


    private String addressLine1;

    private String addressWardName;

    private String addressDistrictName;

    private String addressProvinceName;

    public String fullAddress() {
        String addressDto = this.addressLine1 == null ? "" : this.addressLine1;
        if (StringUtils.hasText(this.addressWardName)) addressDto += " - " + this.addressWardName;
        if (StringUtils.hasText(this.addressDistrictName)) addressDto += " - " + this.addressDistrictName;
        if (StringUtils.hasText(this.addressProvinceName)) addressDto += " - " + this.addressProvinceName;
        return addressDto;
    }

}
