package com.stg.service.dto.quotation.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { OccupationValidator.class })
@Documented
public @interface Occupation {
	String message() default "must not be occupation";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
