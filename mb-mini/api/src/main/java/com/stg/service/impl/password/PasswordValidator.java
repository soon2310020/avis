package com.stg.service.impl.password;

public interface PasswordValidator {
    ValidationResult validate(ValidationInput input, ValidationRules rules);
}
