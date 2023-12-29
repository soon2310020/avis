package com.stg.service.dto.potentialcustomer;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.stg.constant.Gender;
import com.stg.constant.IdentificationType;
import com.stg.entity.potentialcustomer.PotentialCustomer;
import com.stg.service.dto.RawData;
import com.stg.service3rd.mbal.dto.FlexibleCommon;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class PotentialCustomerDto {
    private Long id;
    private String crmId;
    private String fullName;
    private IdentificationType identificationType;
    private String identificationId;
    private LocalDate dob;
    private Gender gender;
    private String email;
    private String phoneNumber;
    private Integer occupationId;
    private String occupation;
    private String note;
    private BigDecimal inputAmount;
    private RawData caredProduct;
    private LocalDate createdDate;
    
    private FlexibleCommon.ReferrerInput rmInfo;
    private FlexibleCommon.ReferrerInput icInfo;

    private String cif;
    
    public static PotentialCustomerDto of(PotentialCustomer entity) {
        PotentialCustomerDto dto = new PotentialCustomerDto();
        dto.setId(entity.getId());
        dto.setCrmId(entity.getCrmId());
        dto.setFullName(entity.getFullName());
        dto.setIdentificationType(entity.getIdentificationType());
        dto.setIdentificationId(entity.getIdentificationId());
        dto.setDob(entity.getDob());
        dto.setGender(entity.getGender());
        dto.setEmail(entity.getEmail());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setOccupationId(entity.getOccupationId());
        dto.setNote(entity.getNote());
        dto.setInputAmount(entity.getInputAmount());
//        dto.setCaredProduct(new RawData(entity.getCaredProduct()));
        dto.setCreatedDate(entity.getCreatedDate().toLocalDate());
        return dto;
    }
}
