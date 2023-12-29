package com.stg.service3rd.mbal.dto;

public enum MaritalStatus {
    SINGLE(1),
    MARRIED(2)
    ;

    private final int value;

    MaritalStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
