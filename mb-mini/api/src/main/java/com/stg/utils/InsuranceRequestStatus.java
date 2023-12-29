package com.stg.utils;

import lombok.Getter;

@Getter
public enum InsuranceRequestStatus {
    FALSE("Từ chối");

    public final String label;

    private InsuranceRequestStatus(String label) {
        this.label = label;
    }
}
