package vn.com.twendie.avis.api.core.constraint;

import vn.com.twendie.avis.api.core.validation.AllNullOrNotValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = AllNullOrNotValidator.class)
@Repeatable(AllNullOrNot.List.class)
public @interface AllNullOrNot {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String[] fields() default {};

    @Target(TYPE)
    @Retention(RUNTIME)
    @interface List {
        AllNullOrNot[] value();
    }

}
