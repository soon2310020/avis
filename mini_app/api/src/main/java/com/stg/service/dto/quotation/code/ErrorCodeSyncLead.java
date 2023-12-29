package com.stg.service.dto.quotation.code;

public enum ErrorCodeSyncLead {
    SUCCESS("000", "Thành công"),
    ID_NOT_FOUND("001", "Lead ID không tồn tại"),
    BAD_REQUEST("400", "Dữ liệu đầu vào không hợp lệ"),
    INTERNAL_SERVER_ERROR("500", "Có lỗi xảy ra, xin vui lòng thử lại"),
    ;
    private final String code;
    private final String message;

    ErrorCodeSyncLead(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

