package vn.com.twendie.avis.api.core.validation;

import vn.com.twendie.avis.api.core.constraint.FromToday;
import vn.com.twendie.avis.api.core.util.DateUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.sql.Timestamp;
import java.util.Objects;

public class FromTodayValidator implements ConstraintValidator<FromToday, Timestamp> {

    private DateUtils dateUtils;

    @Override
    public void initialize(FromToday constraintAnnotation) {
        dateUtils = new DateUtils();
    }

    @Override
    public boolean isValid(Timestamp value, ConstraintValidatorContext context) {
        return Objects.nonNull(value) && dateUtils.endOfDay(value).after(dateUtils.startOfToday());
    }

}
