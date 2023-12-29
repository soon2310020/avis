package com.stg.service.dto.constraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ModulusValidator implements ConstraintValidator<Modulus, Number> {

    private long division;
    private long remainder;
    
    @Override
    public void initialize(Modulus constraintAnnotation) {
        this.division = constraintAnnotation.division();
        this.remainder = constraintAnnotation.remainder();
    }
    
    @Override
    public boolean isValid(Number value, ConstraintValidatorContext context) {
        return value != null ? value.longValue() % division == remainder : true;
    }

}
