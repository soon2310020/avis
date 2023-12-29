package com.stg.utils;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum MicGroup {
    COPPER(1, "CT01"),
    SILVER(2, "CT02"),
    GOLD(3, "CT03"),
    PLATINUM(4, "CT04"),
    DIAMOND(5, "CT05");

    public final int group;
    public final String val;

    MicGroup(int group, String val) {
        this.group = group;
        this.val = val;
    }

    public static int getMicGroup(String val) {
        Optional<MicGroup> insuranceCategory = Arrays.stream(MicGroup.values()).filter(micGroup ->
                val.contains(micGroup.getVal())).findFirst();
        return insuranceCategory.map(MicGroup::getGroup).orElse(0);
    }
}
