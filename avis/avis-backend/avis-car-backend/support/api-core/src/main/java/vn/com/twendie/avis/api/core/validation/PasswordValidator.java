package vn.com.twendie.avis.api.core.validation;

import vn.com.twendie.avis.api.core.constraint.Password;
import vn.com.twendie.avis.api.core.util.StrUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class PasswordValidator implements ConstraintValidator<Password, String> {

    private StrUtils strUtils;

    @Override
    public void initialize(Password constraintAnnotation) {
        strUtils = new StrUtils();
    }

    @Override
    public boolean isValid(String input, ConstraintValidatorContext context) {
        try {
            if (Objects.isNull(input)) {
                return true;
            } else {
                String content = strUtils.cleanWordMarks(input);
                return content.matches("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[\\s~`!@#$%^&*()_\\-:\"'?/><.,|\\\\+=]).{8,100}$");
            }
        } catch (Exception e) {
            return false;
        }
    }

}
