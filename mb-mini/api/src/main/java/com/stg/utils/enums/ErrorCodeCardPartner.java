package com.stg.utils.enums;

import lombok.Getter;

public enum ErrorCodeCardPartner {
    SUCCESS("000", "Thành công", "SUCCESS", "SUCCESS"),

    TIME_OUT("002", "Timeout", "TIME_OUT", "Host timeout"),
    INPUT_INVALID("203", "Đầu vào không hợp lệ",
            "INPUT_INVALID", "Input invalid"),
    MOBILE_INVALID("219", "Số điện thoại di động không hợp lệ",
            "MOBILE_INVALID", "Mobile number is invalid"),
    CUSTOMER_NAME_INVALID("279", "Tên khách hàng không hợp lệ",
            "CUSTOMER_NAME_INVALID", "Customer name is invalid."),
    MISSING_CONFIG_INFO("448", "Thiếu thông tin cấu hình",
            "MISSING_CONFIG_INFO", "Missing configuration information"),

    SERVER_ERROR("500", "Lỗi hệ thống", "SERVER_ERROR", "Unknow error"),
    TYPE_INVALID("1011", "Loại không hợp lệ",
            "TYPE_INVALID", "Type is invalid"),

    CARD_NOT_FOUND("2551", "Mã thẻ không tồn tại ",
            "CARD_NOT_FOUND", "CardId is not exist or card closed"),

    CARD_INVALID("2192", "Loại thẻ không hợp lệ",
            "CARD_INVALID", "Card class is invalid"),
    // new

    //
    GET_DATA_FAIL("4103", "Lấy thông tin từ database lỗi",
            "GET_DATA_FAIL", "Fail to get information from database"),
    CARD_TYPE_NO_SUPPORT("4081", "Loại thẻ không hỗ trợ",
            "CARD_TYPE_NO_SUPPORT", "Not support this card type"),
    RESOURCE_NUMBER_VALID("4205", "Số tài nguyên không hợp lệ",
            "RESOURCE_NUMBER_VALID", "Resource number is invalid"),
    CHANNEL_NO_ACCESS("4484", "Kênh không được phép truy cập",
            "CHANNEL_NO_ACCESS", "Channel not allow to access"),
    CUSTOMER_NOT_FOUND("4095", "Khách hàng không tồn tại",
            "CUSTOMER_NOT_FOUND", "Customer is not exist!"),
    REQUEST_ID_INVALID("6009", "Mã yêu cầu không hợp lệ",
            "REQUEST_ID_INVALID", "RequestId is invalid"),
    CARD_ID_INVALID("6017", "Id thẻ không hợp lệ",
            "CARD_ID_INVALID", ""),
    CHANNEL_NO_SUPPORT("6026", "Channel không hỗ trợ",
            "CHANNEL_NO_SUPPORT", "Channel is not supported."),
    RET_REF_NUMBER_INVALID("6027", "Mã GD muốn chuyển đổi trả góp không hợp lệ",
            "RET_REF_NUMBER_INVALID", "RetRefNumber is not valid."),
    MERCHANT_NO_SUPPORT("6028", "Đối tác không hỗ trợ",
            "MERCHANT_NO_SUPPORT", "Merchant is not supported."),
    CART_TYPE_NO_SUPPORT("6029", "Loại thẻ ,kì hạn  không hỗ trợ",
            "CART_TYPE_NO_SUPPORT", "Class card, period or value transaction is not supported."),
    CARD_NOT_VALID("6030", "Loại thẻ không hợp lệ",
            "CARD_NOT_VALID", "Class card is not valid."),
    PERIOD_INVALID("6031", "Kì hạn không hợp lệ",
            "PERIOD_INVALID", "Period is not valid."),
    VALUE_TRANSACTION_INVALID("6032", "Giá trị giao dịch không hợp lệ",
            "VALUE_TRANSACTION_INVALID", "Value transaction is not valid."),
    MERCHANT_INVALID("6033", "Đối tác không hợp lệ",
            "MERCHANT_INVALID", "Merchant is not valid."),
    CONFIG_CONFLICT("6034", "Cấu hình đã tồn tại vui lòng kiểm tra lại !",
            "CONFIG_CONFLICT", "Configuration conflict, please check again."),
    NOT_ENOUGH_CONDITION("6036", "Không đủ điều kiện.",
            "NOT_ENOUGH_CONDITION", "Transaction did not pass condition check"),
    RET_REF_NUMBER_NOT_FOUND("6037", "Mã GD muốn chuyển đổi trả góp không tồn tại",
            "RET_REF_NUMBER_NOT_FOUND", "RRN not found."),
    INSTALL_ELEMENT_NOT_FOUND("6043", "Thành phần ca đặt không tồn tại",
            "INSTALL_ELEMENT_NOT_FOUND", "Installment is not exist."),

    AMOUNT_INVALID("6044", "Giá không hợp lệ", "AMOUNT_INVALID", "Amount is not valid"),
    INSTALL_REQUEST_ID_NOT_FOUND("6045", "Mã yêu cầu không tồn tại",
            "INSTALL_REQUEST_ID_NOT_FOUND", "Installment request is not exist."),
    REGIS_DATE_INVALID("6046", "Ngày đăng ký không hợp lệ",
            "REGIS_DATE_INVALID", "Register date is not valid"),

    INSTALL_REQUEST_ID_NOT_FOUND_SEVEN_DAY("6047", "Mã yêu cầu không tồn tại trong vòng 7 ngày",
            "INSTALL_REQUEST_ID_NOT_FOUND_SEVEN_DAY", "Installment requet is not exist whithin seven days."),
    CUSTOMER_NAME_INVALID2("6082", "Tên khách hàng không hợp lệ ",
            "CUSTOMER_NAME_INVALID2", "Customer name is invalid."),
    SOURCE_MONEY_INVALID("2894", "Nguồn tiền giao dịch là Tài khoản thanh toán không cho phép trả góp.",
            "SOURCE_MONEY_INVALID", "The source of the transaction money is a Payment Account"),

    ////// status
    INSTALLMENT_SUCCESS("1000", "Đăng ký trả góp thành công",
            "INSTALLMENT_SUCCESS", "INSTALLMENT_SUCCESS"),
    INSTALLMENT_FAILED("1001", "Đăng ký trả góp chưa thành công",
            "INSTALLMENT_FAILED", "INSTALLMENT_FAILED"),
    INSTALLMENT_UNQUALIFIED("1002", "Chưa đủ điều kiện đăng ký trả góp",
            "INSTALLMENT_UNQUALIFIED", "INSTALLMENT_UNQUALIFIED"),
    INSTALLMENT_UNQUALIFIED_CANCEL("1003", "Không đủ điều kiện đăng ký. Hủy đăng ký",
            "INSTALLMENT_UNQUALIFIED_CANCEL", "INSTALLMENT_UNQUALIFIED_CANCEL"),
    ENOUGH_CONDITION("000", "Đủ điều kiện.", "ENOUGH_CONDITION",
            "Transaction passed condition check");

    @Getter
    private String code;
    @Getter
    private String labelVn;
    @Getter
    private String enumEn;
    @Getter
    private String labelEn;

    ErrorCodeCardPartner(String code, String labelVn, String enumEn, String labelEn) {
        this.code = code;
        this.labelVn = labelVn;
        this.enumEn = enumEn;
        this.labelEn = labelEn;
    }

    public static String getEnum(String code) {
        for (ErrorCodeCardPartner item : ErrorCodeCardPartner.values()) {
            if (item.code.equalsIgnoreCase(code)) {
                return item.getEnumEn();
            }
        }
        return null;
    }
}
