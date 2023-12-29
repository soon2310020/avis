package com.stg.service.dto.constraint;

import java.time.LocalDate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DobValidator implements ConstraintValidator<Dob, LocalDate> {

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        return value != null ? !value.isAfter(LocalDate.now()) : true;
    }

}
