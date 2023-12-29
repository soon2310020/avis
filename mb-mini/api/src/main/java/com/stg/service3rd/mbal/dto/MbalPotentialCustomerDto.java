package com.stg.service3rd.mbal.dto;

import javax.validation.constraints.NotBlank;

import com.stg.utils.DateUtil;
import com.stg.utils.FlexibleCommon;
import com.stg.utils.Gender;
import com.stg.utils.IdentificationType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class MbalPotentialCustomerDto {

    public static final String MALE = "1";
    public static final String FEMALE = "2";

    public static final String BUSINESS_MODEL_REFER = "1";

    public static final String BUSINESS_MODEL_DIRECT = "2";

    public static final String NATIONAL_ID = "1";

    public static final String CITIZEN_ID = "2";

    public static final String PASSPORT = "3";

    public static final String MILITARY_ID = "4";

    @NotBlank
    private String fullName;
    @NotBlank
    private String identityType;
    @NotBlank
    private String identityNumber;
    @NotBlank
    private String dob;
    private String email;
    @NotBlank
    private String phoneNumber;
    private String address;
    @NotBlank
    private String occupation;
    @NotBlank
    private String gender;
    private MaritalStatus married;
    @NotBlank
    private String cifNumber;
    @NotBlank
    private String businessModel;

    public void setIdentityType(IdentificationType type) {
        if (type == null) {
            return;
        }
        switch (type) {
            case NATIONAL_ID:
                identityType = NATIONAL_ID;
                break;
            case CITIZEN_ID:
                identityType = CITIZEN_ID;
                break;
            case PASSPORT:
                identityType = PASSPORT;
                break;
            case MILITARY_ID:
                identityType = MILITARY_ID;
                break;
            default:
                break;
        }
    }

    public static MbalPotentialCustomerDto of(FlexibleCommon.CustomerInfo customer, String cif) {
        MbalPotentialCustomerDto customerDto = new MbalPotentialCustomerDto();

        customerDto.setFullName(customer.getFullName());
        customerDto.setIdentityType(customer.getIdentificationType());
        customerDto.setIdentityNumber(customer.getIdentificationNumber());
        customerDto.setDob(DateUtil.convertFormat(customer.getDob(), DateUtil.DATE_YYYY_MM_DD, DateUtil.DATE_YMD));
        customerDto.setGender(customer.getGender() == Gender.MALE ? MALE : FEMALE);
        customerDto.setEmail(customer.getEmail());
        customerDto.setPhoneNumber(customer.getPhoneNumber());

        customerDto.setOccupation(customer.getOccupationId().toString());

        if (customer.getAddress() != null) {
            customerDto.setAddress(customer.getAddress().fullAddress());
        }

        customerDto.setMarried(null);
        customerDto.setCifNumber(cif);
        customerDto.setBusinessModel(MbalPotentialCustomerDto.BUSINESS_MODEL_DIRECT);
        return customerDto;
    }
    
}
