package com.stg.service.impl.password;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.BooleanUtils;
import org.passay.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PassayPasswordValidator implements PasswordValidator {

    @Override
    public ValidationResult validate(ValidationInput input, ValidationRules rules) {
        PasswordData passwordData = new PasswordData(input.getPassword());
        List<Rule> passayRules = new ArrayList<>();

        if (BooleanUtils.isTrue(rules.getMustNotContainUsernameEnabled())) {
            passayRules.add(new UsernameRule(MatchBehavior.Contains));
            passwordData = new PasswordData(input.getEmail(), input.getPassword());
        }

        if (rules.getMinimumLength() != null) {
            passayRules.add(new LengthRule(rules.getMinimumLength(), rules.getMaximumLength()));
        }

        if (rules.getMinimumLowerCaseCount() != null) {
            passayRules.add(new CharacterRule(EnglishCharacterData.LowerCase, rules.getMinimumLowerCaseCount()));
        }

        if (rules.getMinimumUpperCaseCount() != null) {
            passayRules.add(new CharacterRule(EnglishCharacterData.UpperCase, rules.getMinimumUpperCaseCount()));
        }

        if (rules.getMinimumNumberCount() != null) {
            passayRules.add(new CharacterRule(EnglishCharacterData.Digit, rules.getMinimumNumberCount()));
        }

        if (rules.getMaximumRepeatingCharactersCount() != null) {
            passayRules.add(new RepeatCharacterRegexRule(rules.getMaximumRepeatingCharactersCount()));
        }

        if (rules.getMinimumSymbolCount() != null) {
            passayRules.add(new CharacterRule(EnglishCharacterData.Special, rules.getMinimumSymbolCount()));
        }

        org.passay.PasswordValidator passwordValidator = new org.passay.PasswordValidator(passayRules);
        RuleResult result = passwordValidator.validate(passwordData);

        return new ValidationResult()
                .setValid(result.isValid())
                .setValidationMessages(createResultMessages(result));
    }

    private List<String> createResultMessages(RuleResult result) {
        return result.getDetails()
                .stream()
                .map(RuleResultConverter::convertRuleResultDetail)
                .collect(Collectors.toList());
    }
}
