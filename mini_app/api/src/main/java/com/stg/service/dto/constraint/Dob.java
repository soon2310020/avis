package com.stg.service.dto.constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { DobValidator.class })
public @interface Dob {

    String message() default "Ngày sinh không được lớn hơn ngày hiện tại";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    
}
