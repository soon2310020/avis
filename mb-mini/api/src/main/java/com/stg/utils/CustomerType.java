package com.stg.utils;

import lombok.Getter;

@Getter
public enum CustomerType {
    POLICY_HOLDER("Người mua bảo hiểm"),
    INSURED("Người được bảo hiểm");

    public final String label;

    private CustomerType(String label) {
        this.label = label;
    }
}
