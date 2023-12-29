package vn.com.twendie.avis.mobile.api.constraint;

import vn.com.twendie.avis.mobile.api.validation.NotNullTogetherValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = NotNullTogetherValidator.class)
@Repeatable(NotNullTogether.List.class)
public @interface NotNullTogether {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String[] fields() default {};

    @Target(TYPE)
    @Retention(RUNTIME)
    @interface List {
        NotNullTogether[] value();
    }

}
