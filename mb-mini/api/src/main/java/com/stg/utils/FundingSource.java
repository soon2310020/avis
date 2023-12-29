package com.stg.utils;

import lombok.Getter;

@Getter
public enum FundingSource {
    ACCOUNT("ACCOUNT"),
    CARD("CARD");

    public final String label;

    private FundingSource(String label) {
        this.label = label;
    }

}
