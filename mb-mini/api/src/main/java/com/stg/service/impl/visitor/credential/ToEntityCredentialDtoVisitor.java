package com.stg.service.impl.visitor.credential;

import com.stg.entity.credential.Credential;
import com.stg.entity.credential.EmailAndPasswordCredential;
import com.stg.errors.PasswordValidationFailedException;
import com.stg.service.dto.credential.CredentialDtoVisitor;
import com.stg.service.dto.credential.EmailAndPasswordCredentialDto;
import com.stg.service.impl.password.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Creates the corresponding credential entity from a credential dto object.
 */
@Component
@RequiredArgsConstructor
public class ToEntityCredentialDtoVisitor implements CredentialDtoVisitor<Credential> {

    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;
    private final boolean validatePassword;

    @Autowired
    public ToEntityCredentialDtoVisitor(PasswordEncoder passwordEncoder, PasswordValidator passwordValidator) {
        this(passwordEncoder, passwordValidator, true);
    }

    public ToEntityCredentialDtoVisitor(PasswordEncoder passwordEncoder) {
        this(passwordEncoder, null, false);
    }

    @Override
    public EmailAndPasswordCredential visit(EmailAndPasswordCredentialDto dto) {
        EmailAndPasswordCredential usernameAndPasswordCredential = new EmailAndPasswordCredential();

        if (validatePassword) {
            validatePassword(createValidationInput(dto), PasswordRequirements.DEFAULT);
        }

        usernameAndPasswordCredential.setNonExpired(dto.getNonExpired());
        usernameAndPasswordCredential.setEmail(dto.getEmail());
        usernameAndPasswordCredential.setPassword(passwordEncoder.encode(dto.getPassword()));

        return usernameAndPasswordCredential;
    }

    private void validatePassword(ValidationInput input, ValidationRules rules) {
        ValidationResult result = passwordValidator.validate(input, rules);

        if (!result.isValid()) {
            List<String> validationMessages = result.getValidationMessages();
            String message = String.join(",", validationMessages);

            throw new PasswordValidationFailedException(message);
        }
    }

    private static ValidationInput createValidationInput(EmailAndPasswordCredentialDto dto) {
        return new ValidationInput()
                .setEmail(dto.getEmail())
                .setPassword(dto.getPassword());
    }

}
