package com.stg.service.dto.constraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.stg.constant.IdentificationType;
import com.stg.service.dto.IdentificationDto;

public class IdentificationValidator implements ConstraintValidator<Identification, IdentificationDto> {
    
    @Override
    public boolean isValid(IdentificationDto value, ConstraintValidatorContext context) {
        IdentificationType identificationType = value.getIdentificationType();
        String identificationId = value.getIdentificationId();
        if (identificationType == null || identificationId == null) return false;

        switch (identificationType) {
            case NATIONAL_ID:
                return identificationId.length() >= 9 && identificationId.length() <= 12;
            case CITIZEN_ID:
                return identificationId.length() == 12;
            case MILITARY_ID:
                return identificationId.length() >= 8 && identificationId.length() <= 12;
            case BIRTH_CERTIFICATE:
                return identificationId.length() >= 3 && identificationId.length() <= 20;
            case PASSPORT:
                return identificationId.length() >= 8;
            default:
                return false;
        }
    }

}
