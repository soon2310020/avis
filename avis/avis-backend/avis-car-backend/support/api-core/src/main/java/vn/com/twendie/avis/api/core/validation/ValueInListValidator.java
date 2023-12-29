package vn.com.twendie.avis.api.core.validation;

import vn.com.twendie.avis.api.core.constraint.ValueInList;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

public class ValueInListValidator implements ConstraintValidator<ValueInList, Object> {

    private List<String> values;

    @Override
    public void initialize(ValueInList constraintAnnotation) {
        values = Arrays.asList(constraintAnnotation.values());
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        return values.contains(String.valueOf(value));
    }

}
