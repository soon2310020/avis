package com.stg.service.dto.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { LessValidator.class })
@Documented
public @interface Less {
	
	String field();
    String dependField();
    boolean orEqual() default false;
	
	String message() default "{Less.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
