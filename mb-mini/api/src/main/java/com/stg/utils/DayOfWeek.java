package com.stg.utils;

import lombok.Getter;

@Getter
public enum DayOfWeek {
    SUNDAY(0),
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6);

    public final int dayNum;

    private DayOfWeek(int dayNum) {
        this.dayNum = dayNum;
    }
}
