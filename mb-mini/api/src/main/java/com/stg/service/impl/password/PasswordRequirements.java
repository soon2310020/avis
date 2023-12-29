package com.stg.service.impl.password;

public class PasswordRequirements {
    public static final ValidationRules DEFAULT = new ValidationRules()
            .setMaximumLength(45)
            .setMinimumLength(8)
            .setMinimumLowerCaseCount(1)
            .setMinimumUpperCaseCount(1)
            .setMinimumNumberCount(1)
			.setMinimumSymbolCount(1)
            .setMustNotContainUsernameEnabled(true)
            .setMaximumRepeatingCharactersCount(3);

    public static final ValidationRules BAAS_TRANSACTION_RULE = new ValidationRules()
            .setMaximumLength(8)
            .setMinimumLength(8)
            .setMinimumLowerCaseCount(1)
            .setMinimumNumberCount(1);

}
