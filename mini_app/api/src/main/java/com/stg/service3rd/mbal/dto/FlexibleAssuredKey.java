package com.stg.service3rd.mbal.dto;

import com.stg.constant.Gender;
import com.stg.constant.IdentificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class FlexibleAssuredKey {

    private String fullName;
    private String dob;
    private IdentificationType identificationType;
    private String identificationNumber;
    private Gender gender;

    public static FlexibleAssuredKey of(String fullName, String dob, Gender gender, IdentificationType identificationType, String identificationNumber) {
        return new FlexibleAssuredKey()
                .setFullName(fullName)
                .setDob(dob)
                .setIdentificationType(identificationType)
                .setIdentificationNumber(identificationNumber)
                .setGender(gender);
    }

    public FlexibleAssuredKey(FlexibleCommon.Assured assured) {
                setFullName(assured.getFullName());
                setDob(assured.getDob());
                setIdentificationType(assured.getIdentificationType());
                setIdentificationNumber(assured.getIdentificationNumber());
                setGender(assured.getGender());
    }

    public FlexibleAssuredKey(FlexibleCommon.PolicyHolderAndLifeAssured assured) {
        setFullName(assured.getFullName());
        setDob(assured.getDob());
        setIdentificationType(assured.getIdentificationType());
        setIdentificationNumber(assured.getIdentificationNumber());
        setGender(assured.getGender());
    }

}
