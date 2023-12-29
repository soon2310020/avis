package com.stg.utils;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum InsuranceCategory {
    HEALTHY("5 năm"),
    HAPPY("10 năm");

    public final String label;

    private InsuranceCategory(String label) {
        this.label = label;
    }

    public static String getInsuranceCategoryVal(String category) {
        Optional<InsuranceCategory> insuranceCategory = Arrays.stream(InsuranceCategory.values()).filter(ic ->
                ic.name().equals(category)).findFirst();
        if (insuranceCategory.isPresent()) {
            return insuranceCategory.get().getLabel();
        }
        return "Not found";
    }
}
