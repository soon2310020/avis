package com.stg.utils;

import lombok.Getter;

@Getter
public enum InsuranceContractStatus {
    TRUE("Đang hiệu lực");

    public final String label;

    private InsuranceContractStatus(String label) {
        this.label = label;
    }
}
