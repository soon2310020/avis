package com.stg.entity.lead;

import org.springframework.util.StringUtils;

public enum LeadStatus {

    S_1("1", "Tạo mới"),
    S_2("2", "Chờ xác nhận"),
    S_3("3", "Đã nhận"),
    S_4("4", "Gọi điện/Email"),
    S_5("5", "Trả lại"),
    S_6("6", "Đã gặp"),
    S_7("7", "Đã nộp hsycbh"),
    S_8("8", "Đã phát hành"),
    S_9("10", "Chờ phân giao RM"),
    S_10("11", "RM trả lại"),
    S_11("13", "KH Từ chối"),

    ;
    private String code;
    private String description;

    private LeadStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static LeadStatus codeOf(String code) {
        if (StringUtils.hasText(code)) {
            for (LeadStatus s : LeadStatus.values()) {
                if (s.code.equalsIgnoreCase(code)) {
                    return s;
                }
            }
        }
        return null;
    }
}
