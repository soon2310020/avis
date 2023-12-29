package com.stg.service.dto.campaign;

import lombok.Getter;

@Getter
public enum CampaignStatus {
    NOT_STARTED("Chưa bắt đầu"),
    IN_PROGRESS("Đang diễn ra"),
    ENDED("Đã kết thúc");

    public final String label;

    private CampaignStatus(String label) {
        this.label = label;
    }
}
