package com.stg.service3rd.mbal.dto;

import com.stg.constant.Gender;
import com.stg.constant.IdentificationType;
import com.stg.entity.potentialcustomer.PotentialCustomer;
import com.stg.entity.quotation.QuotationCustomer;
import com.stg.utils.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

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
    //    @NotBlank
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

    /**
     * flow: refer
     */
    public static MbalPotentialCustomerDto of(PotentialCustomer potentialCustomer, String businessModel) {
        MbalPotentialCustomerDto customerDto = new MbalPotentialCustomerDto();
        customerDto.setFullName(potentialCustomer.getFullName());
        customerDto.setIdentityType(potentialCustomer.getIdentificationType());
        customerDto.setIdentityNumber(potentialCustomer.getIdentificationId());
        customerDto.setDob(DateUtil.localDateTimeToString(potentialCustomer.getDob(), DateUtil.DATE_YMD));
        customerDto.setGender(potentialCustomer.getGender() == Gender.MALE ? MALE : FEMALE);
        customerDto.setEmail(potentialCustomer.getEmail());
        customerDto.setPhoneNumber(potentialCustomer.getPhoneNumber());

        customerDto.setOccupation(Integer.toString(potentialCustomer.getOccupationId()));

        customerDto.setAddress(null);
        customerDto.setMarried(null);
        customerDto.setCifNumber(null);
        customerDto.setBusinessModel(businessModel);
        return customerDto;
    }

    /**
     * flow: direct
     */
    public static MbalPotentialCustomerDto of(QuotationCustomer customer, String cif) {
        MbalPotentialCustomerDto customerDto = new MbalPotentialCustomerDto();

        customerDto.setFullName(customer.getFullName());
        customerDto.setIdentityType(customer.getIdentificationType());
        customerDto.setIdentityNumber(customer.getIdentificationId());
        customerDto.setDob(DateUtil.localDateTimeToString(customer.getDob(), DateUtil.DATE_YMD));
        customerDto.setGender(customer.getGender() == Gender.MALE ? MALE : FEMALE);
        customerDto.setEmail(customer.getEmail());
        customerDto.setEmail(null);
        customerDto.setPhoneNumber(customer.getPhoneNumber());

        customerDto.setOccupation(customer.getOccupationId().toString());

        customerDto.setAddress(customer.fullAddress());

        customerDto.setMarried(null);
        customerDto.setCifNumber(cif);
        customerDto.setBusinessModel(MbalPotentialCustomerDto.BUSINESS_MODEL_DIRECT);
        return customerDto;
    }

}
