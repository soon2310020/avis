package com.stg.utils;

import lombok.Getter;

@Getter
public enum CardLabel {
    LIMIT_NOT_ENOUGH("Không đủ hạn mức"),
    CARD_EXPIRED("Hiệu lực thẻ không đáp ứng thời gian trả góp"),
    PASS("Pass");

    public final String label;

    CardLabel(String label) {
        this.label = label;
    }
}
