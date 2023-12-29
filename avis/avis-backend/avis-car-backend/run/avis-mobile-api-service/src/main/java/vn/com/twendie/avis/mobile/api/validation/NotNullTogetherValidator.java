package vn.com.twendie.avis.mobile.api.validation;

import lombok.SneakyThrows;
import vn.com.twendie.avis.mobile.api.constraint.NotNullTogether;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class NotNullTogetherValidator implements ConstraintValidator<NotNullTogether, Object> {

    private List<String> fields;

    @Override
    public void initialize(NotNullTogether constraintAnnotation) {
        fields = new ArrayList<>(Arrays.asList(constraintAnnotation.fields()));
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            return fields.stream()
                    .map(field -> getFieldValue(value, field))
                    .anyMatch(Objects::nonNull);
        } catch (Exception e) {
            return false;
        }
    }

    @SneakyThrows
    private Object getFieldValue(Object o, String fieldName) {
        Field field = o.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(o);
    }
}
