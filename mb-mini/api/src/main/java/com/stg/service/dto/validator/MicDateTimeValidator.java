package com.stg.service.dto.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MicDateTimeValidator implements ConstraintValidator<MicDateTimeFormat, String> {

    private static final String FORMAT = "dd/MM/yyyy";
    private static final String MESSAGE_WRONG_FORMAT = "Value has wrong format " + FORMAT;
    private static final String MESSAGE_WRONG_VALUE = "Value must be in pass";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        Date valueDate = generateDate(value);
        if (valueDate == null) {
            context.buildConstraintViolationWithTemplate(MESSAGE_WRONG_FORMAT).addConstraintViolation();
            return false;
        }
        context.buildConstraintViolationWithTemplate(MESSAGE_WRONG_VALUE).addConstraintViolation();
        return valueDate.before(new Date());
    }

    private static Date generateDate(String value) {
        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(FORMAT);
            if (value != null) {
                date = sdf.parse(value);
                if (!value.equals(sdf.format(date))) {
                    date = null;
                }
            }

        } catch (ParseException ignored) {
            return null;
        }
        return date;
    }
}
