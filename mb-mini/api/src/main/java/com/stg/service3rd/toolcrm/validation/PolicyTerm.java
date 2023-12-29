package com.stg.service3rd.toolcrm.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { PolicyTermValidator.class })
@Documented
public @interface PolicyTerm {
	
	String message() default "policy term must be less than or equal to 100 years old";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
