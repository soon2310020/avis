package com.stg.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;


public class CommonUtils {

    public static final List<Double> MIN_BASE_PREMIUM_RATE  = Arrays.asList(
            7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0, 7.0,
            7.0, 7.0, 7.0, 7.0, 7.0, 7.5, 8.0, 8.0, 8.5, 9.0, 9.5, 10.0, 10.5, 11.0, 11.5, 12.0, 12.5, 13.0, 14.0, 14.5, 15.5,
            16.0, 17.0, 18.0, 19.0, 20.0, 21.0, 22.0, 23.5, 24.5, 26.0, 27.5, 29.0, 30.5, 32.5, 34.0, 36.0, 38.0, 40.0, 42.0,
            44.5, 47.5, 50.0);

    public static int limitedTimes(int ages) {
        if (ages > 60) return 10;
        else if (ages >= 51) return 15;
        return 25;
    }

    public static BigDecimal getValidateMinSA(BigDecimal amount) {
        return (amount.multiply(BigDecimal.valueOf(1000))).divide(BigDecimal.valueOf(200), RoundingMode.HALF_UP);
    }

    public static BigDecimal getValidateMaxSA(int ages, BigDecimal amount) {
        return (amount.multiply(BigDecimal.valueOf(1000))).divide(BigDecimal.valueOf(MIN_BASE_PREMIUM_RATE.get(ages)), RoundingMode.HALF_UP);
    }

    public static BigDecimal maxSumAssured(int ages, BigDecimal annualIncome) {
        return annualIncome.multiply(BigDecimal.valueOf(limitedTimes(ages)));
    }

    public static String replaceWithAsterisks(String input, int start, int end) {
        if (input == null || input.isEmpty() || start >= end || start < 0 ) {
            return input;
        }
        StringBuilder result = new StringBuilder(input);
        for (int i = start; i <= end; i++) {
            if (i > input.length() - 1) {
                break;
            }
            result.setCharAt(i, '*');
        }
        return result.toString();
    }
}
