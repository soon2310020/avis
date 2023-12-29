package com.stg.utils;

import lombok.Getter;

@Getter
public enum ContractStatusType {
    ACTIVE("Hiệu lực"),
    REVERSED("Huỷ"),
    LAPSE("Mất hiệu lực"),
    DORMANT("Mất hiệu lực hoàn toàn"),
    PENDING("Đang xử lý"),
    EMPTY(""),
    ;
    private final String text;

    ContractStatusType(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }
}
