package com.stg.service.impl.password;

import org.passay.RuleResultDetail;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class RuleResultConverter {
    private static final String TOO_SHORT_MESSAGE = "Mật khẩu từ 8-12 ký tự";
    private static final String INSUFFICIENT_DIGIT_MESSAGE = "Có ít nhất %d ký tự số";
    private static final String INSUFFICIENT_UPPERCASE_MESSAGE = "Có ít nhất %d chữ cái viết hoa";
    private static final String INSUFFICIENT_LOWERCASE_MESSAGE = "Có ít nhất %d chữ cái viết thường";
    private static final String ILLEGAL_USERNAME_MESSAGE = "The password must not contain the username";
    private static final String MAXIMUM_REPEATING_CHARACTERS_MESSAGE = "The password must not contain repeating characters";
    private static final String HISTORY_VIOLATION_MESSAGE = "The password must be different than your previous passwords";
    private static final String SPECIAL_CHARACTER_MESSAGE = "Có ít nhất %d ký tự đặc biệt";
    private static final String MINIMUM_REQUIRED = "minimumRequired";

    private static Map<String, Function<RuleResultDetail, String>> errorCodeConvertMap;

    static {
        Map<String, Function<RuleResultDetail, String>> map = new HashMap<>();

        map.put("TOO_SHORT", RuleResultConverter::convertTooShortResult);
        map.put("INSUFFICIENT_UPPERCASE", RuleResultConverter::convertInsufficientUppercaseResult);
        map.put("INSUFFICIENT_DIGIT", RuleResultConverter::convertInsufficientDigitResult);
        map.put("INSUFFICIENT_LOWERCASE", RuleResultConverter::convertInsufficientLowercaseResult);
        map.put("ILLEGAL_USERNAME", RuleResultConverter::convertIllegalUsernameResult);
        map.put("ILLEGAL_MATCH", RuleResultConverter::convertMaximumRepeatingCharactersResult);
        map.put("HISTORY_VIOLATION", RuleResultConverter::convertHistoryViolationResult);
        map.put("INSUFFICIENT_SPECIAL", RuleResultConverter::convertSpecialCharacterResult);

        errorCodeConvertMap = Collections.unmodifiableMap(map);
    }

    public static String convertRuleResultDetail(RuleResultDetail ruleResultDetail) {
        Function<RuleResultDetail, String> ruleResultDetailStringFunction =
                errorCodeConvertMap.get(ruleResultDetail.getErrorCode());

        if (ruleResultDetailStringFunction == null) {
            throw new IllegalStateException("Unknown rule for error code=" + ruleResultDetail.getErrorCode());
        }

        return ruleResultDetailStringFunction.apply(ruleResultDetail);
    }

    private static String convertHistoryViolationResult(RuleResultDetail ruleResultDetail) {
        return HISTORY_VIOLATION_MESSAGE;
    }

    private static String convertMaximumRepeatingCharactersResult(RuleResultDetail ruleResultDetail) {
        return MAXIMUM_REPEATING_CHARACTERS_MESSAGE;
    }

    private static String convertInsufficientLowercaseResult(RuleResultDetail ruleResultDetail) {
        Map<String, Object> parameters = ruleResultDetail.getParameters();

        return String.format(INSUFFICIENT_LOWERCASE_MESSAGE, parameters.get(MINIMUM_REQUIRED));
    }

    private static String convertIllegalUsernameResult(RuleResultDetail ruleResultDetail) {
        return ILLEGAL_USERNAME_MESSAGE;
    }

    private static String convertTooShortResult(RuleResultDetail ruleResultDetail) {
        Map<String, Object> parameters = ruleResultDetail.getParameters();

        return String.format(TOO_SHORT_MESSAGE, parameters.get("minimumLength"));
    }

    private static String convertInsufficientUppercaseResult(RuleResultDetail ruleResultDetail) {
        Map<String, Object> parameters = ruleResultDetail.getParameters();

        return String.format(INSUFFICIENT_UPPERCASE_MESSAGE, parameters.get(MINIMUM_REQUIRED));
    }

    private static String convertInsufficientDigitResult(RuleResultDetail ruleResultDetail) {
        Map<String, Object> parameters = ruleResultDetail.getParameters();
        return String.format(INSUFFICIENT_DIGIT_MESSAGE, parameters.get(MINIMUM_REQUIRED));
    }

    private static String convertSpecialCharacterResult(RuleResultDetail ruleResultDetail) {
        Map<String, Object> parameters = ruleResultDetail.getParameters();
        return String.format(SPECIAL_CHARACTER_MESSAGE, parameters.get(MINIMUM_REQUIRED));
    }
}
