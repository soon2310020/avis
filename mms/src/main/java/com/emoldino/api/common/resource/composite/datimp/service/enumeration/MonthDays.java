package com.emoldino.api.common.resource.composite.datimp.service.enumeration;

import lombok.Getter;

@Getter
public enum MonthDays {
    FIRST("1st"), SECOND("2nd"), THIRD("3rd"), FORTH("4th");

    private final String title;

    MonthDays(String title) { //
        this.title = title;
    }
}
