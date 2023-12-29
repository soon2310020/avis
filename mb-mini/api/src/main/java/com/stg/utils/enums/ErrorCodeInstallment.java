package com.stg.utils.enums;

import lombok.Getter;

public enum ErrorCodeInstallment {
    SUCCESS("000", "Thành công"),
    TIME_OUT("002", "Host timeout"),
    ACCOUNT_NOT_FOUND("200", "Tài khoản không tồn tại trên hệ thống T24"),
    INPUT_INVALID("203", "Đầu vào không hợp lệ"),
    AMOUNT_INVALID("209", "Số tiền không hợp lệ"),

    BULK_ID_NOT_FOUND("402", "Mã id lô không tồn tại trên hệ thống"),

    SERVER_ERROR("500", "Lỗi không xác định"),
    REQUEST_PAYMENT_FAIL("503", "Yêu cầu thanh toán không thành công"),
    SERVICE_TYPE_INVALID("2030", "Loại dịch vụ không hợp lệ"),

    CREDIT_CARD_INVALID("2870", "Tài khoản tín dụng không hợp lệ"),
    REQUEST_ID_NOT_FOUND("4019", "Mã yêu cầu không tồn tại"),
    REQUEST_ID_INVALID("6009", "Mã yêu cầu không hợp lệ"),

    REGIS_NUMBER_INVALID("20033", "Số đăng ký không hợp lệ"),
    SOURCE_TYPE_INVALID("20034", " Loại nguồn không hợp lệ"),
    SOURCE_NUMBER_INVALID("20035", " Số nguồn không hợp lệ"),
    SOURCE_NAME_INVALID("20036", "Nguồn tên không hợp lệ"),
    AUTHEN_TYPE_INVALID("20037", "Loại xác thực không hợp lệ"),

    EMAIL_INVALID("200138", "Email không hợp lệ"),
    DEEP_LINK_INVALID("20039", "Deeplink không hợp lệ"),

    DEEP_LINK_AUTHEN_INVALID("20040", "Deeplink xác thực không hợp lệ"),

    APPLICATION_TYPE("20041", "Loại ứng dụng không hợp lệ"),
    OTP_INVALID("20142", "Otp không hợp lệ"),
    REQUEST_TOKEN_INVALID("20043", "Yêu cầu token không hợp lệ"),
    DEVICE_ID_INVALID("20044", "Mã thiết bị không hợp lệ"),
    SOURCE_ID_INVALID("20045", "Nguồn id không hợp lệ"),
    NUMBER_TRANSACTION_MAX_DAY("24031", "Giá trị giao dịch vượt hạn mức tối đa trong ngày"),
    NUMBER_TRANSACTION_MAX_MONTH("24032", "Giá trị giao dịch vượt quá hạn mức tối đa tháng"),
    FAIL_TO_CHECK_BALANCE("24033", "Không thể kiểm tra giới hạn số dư"),

    NUMBER_TRANSACTION_HAS_REACH("24034", "Số lượng giao dịch mỗi ngày đã đạt"),
    CREDIT_CARD_NO_CONFIG("40013", "Tài khoản tín dụng không được cấu hình"),

    CHANNEL_AUTO_INVALID("40041", "Channel auto config không hợp lệ"),
    SOURCE_NUMBER_INACTIVE("40042", "Nguồn số không hoạt động"),
    TOTAL_TRANSACTION_INVALID("40050", "Số lượng giao dịch không hợp lệ hoặc vượt quá"),

    TOTAL_AMOUNT_INVALID("40051", "Tổng số tiền không hợp lệ hoặc vượt quá thời lượng tối đa"),

    TOTAL_FREE_INVALID("40052", "Tổng phí không hợp lệ hoặc vượt quá thời lượng tối đa"),

    BULK_DETAIL_INVALID("40053", "Chi tiết lô không hợp lệ hoặc vượt quá độ dài tối đa"),

    BULK_ID_INVALID("40054", "Bulk id không hợp lệ hoặc vượt quá độ dài tối đa "),

    TRANSACTION_INVALID("40055", "Yêu cầu giao dịch không hợp lệ"),

    ID_SOURCE_INVALID("40056", "Id nguồn không hợp lệ hoặc vượt quá độ dài tối đa"),

    TRANSFER_CODE_INVALID("40057", "Mã chuyển không hợp lệ hoặc vượt quá độ dài tối đa"),

    TRANSFER_AMOUNT_INVALID("40058", "Số tiền chuyển không hợp lệ hoặc vượt quá thời lượng tối đa"),

    TRANSFER_CONTENT_INVALID("40059", "Chuyển nội dung không hợp lệ hoặc vượt quá độ dài tối đa"),

    DUPLICATE_REQUEST_ID("40061", "Bulk id đã tồn tại"),

    SAVE_DATA_FAIL("54198", "Lưu data thất bại"),
    NO_AUTHEN_METHOD_FOUND("51170", "Không tìm thấy phương thức xác thực nào"),
    TRANSACTION_NOT_FOUND("51173", "Giao dịch không tồn tại"),
    TRANSACTION_EXP("54195", "Giao dịch hết hạn"),
    TOTAL_AVAILABLE_EXP("54052", "Tổng số có sẵn đã hết hạn"),
    GET_OTP_FAIL("54098", "Lấy otp thất bại"),
    VERIFY_OTP_FAIL("54099", "Xác minh OTP không thành công");

    @Getter
    private String code;
    @Getter
    private String labelVn;

    ErrorCodeInstallment(String code, String labelVn) {
        this.code = code;
        this.labelVn = labelVn;
    }

    public static String getEnum(String code) {
        for (ErrorCodeInstallment item : ErrorCodeInstallment.values()) {
            if (item.code.equalsIgnoreCase(code)) {
                return item.getLabelVn();
            }
        }
        return null;
    }
}
