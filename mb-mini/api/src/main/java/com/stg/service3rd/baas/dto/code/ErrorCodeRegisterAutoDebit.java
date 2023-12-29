package com.stg.service3rd.baas.dto.code;

import com.stg.service3rd.common.dto.enumcode.EnumCodeUnknown;
import com.stg.service3rd.common.dto.enumcode.IEnumCode;
import lombok.Getter;

@Getter
public enum ErrorCodeRegisterAutoDebit implements IEnumCode {
    OK("000", "OK", "Thành công"),
    TIME_OUT("002", "Time out", "Quá thời hạn chờ của máy chủ"),
    UNKNOWN_ERROR("500", "Unknown error", "Lỗi không xác định"),
    REGISTER_NUMBER_INVALID("20033", "Registration number invalid", "Mã đăng ký kinh doanh không hợp lệ"),
    SOURCE_TYPE_INVALID("20034", "Source type invalid", "Loại tài khoản không hợp lệ"),
    SOURCE_NUMBER_INVALID("20035", "Source number invalid", "Số tài khoản không hợp lệ"),
    SOURCE_NAME_INVALID("20036", "Source name invalid", "Tên tài khoản không hợp lệ"),
    AUTH_TYPE_INVALID("20037", "Authentication type invalid", "Kiểu xác thực không hợp lệ"),
    EMAIL_INVALID("200138", "Email input invalid", "Địa chỉ email không hợp lệ"),
    APP_TYPE_INVALID("20041", "Application type invalid", "Phân loại ứng dụng không hợp lệ"),
    SOURCE_NUMBER_STILL_ACTIVE("40042", "Source number is still active", "Số tài khoản vẫn còn hiệu lực"),
    OTP_INVALID("20142", "OTP input invalid", "Nhập sai mã OTP"),
    SAVE_DATA_INVALID("54198", "Saved data failed", "Lưu dữ liệu không thành công"),
    ACCOUNT_NOT_FOUND("4422", "Fail to get account info!", "Không lấy được thông tin tài khoản!"),
    CUSTOMER_TYPE_INVALID("3007", "CustomerType invalid", "Đối tượng khách hàng không hợp lệ"),
    NATIONAL_ID_INVALID("40015", "Invalid national id", "Số giấy tờ tùy thân không hợp lệ"),
    PHONE_NUMBER_INVALID("1109", "Phone number invalid or mismatch", "Số điện thoại không khớp hoặc không hợp lệ"),
    ACCOUNT_NOT_EXISTS_T24("200", "Account doesn't exist on T24 system", "Tài khoản không tồn tại trên hệ thống T24"),
    SERVICE_CODE_INVALID("2830", "Service code invalid", "Mã dịch vụ không hợp lệ"),
    CLIENT_MESSAGE_INVALID("4424", "ClientMessage invalid", "Tin nhắn của khách hàng không hợp lệ"),
    SOURCE_NUMBER_CLOSED("40043", "Source account number closed", "Số tài khoản nguồn đã đóng"),
    RESOURCE_ID_INVALID("284", "Input ResourceId is invalid!", "Nhập sai mã tài nguyên"),
    WALLET_ID_NOT_EXISTS("2781", "WalletId doesn't existed", "Số ví điện tử không tồn tại"),
    TRANSACTION_ID_INVALID("24424", "Transactionid invalid", "Mã giao dịch không hợp lệ"),
    CARD_ID_CLOSED("255", "CardId is not exist or card closed", "Mã thẻ đã đóng hoặc không tồn tại"),
    CURRENCY_INVALID("202", "Currency is invalid", "Đơn vị tiền tệ không hợp lệ"),
    AUTH_METHOD_NOT_FOUND("51170", "No authentication methods found", "Không tìm thấy phương thức xác thực"),
    TRANS_NOT_FOUND("51173", "Transaction not found", "Giao dịch không được tìm thấy"),
    TRANS_EXPIRED("54195", "Transaction expired", "Giao dịch đã hết hạn"),
    TOTAL_AVAILABLE_EXPIRED("54052", "Total available expired", "Tổng số lượng hiện có đã hết hạn"),
    REQUEST_ID_NOT_EXISTED("4019", "Request id not existed", "ID được yêu cầu không tồn tại"),
    DEEPLINK_AUTH_INVALID("20040", "Deeplink authentication invalid", "Liên kết xác thực không hợp lệ"),
    ;
    private final String code;
    private final String messageOrigin;
    private final String messageVn;

    /***/
    ErrorCodeRegisterAutoDebit(String code, String messageOrigin) {
        this.code = code;
        this.messageOrigin = messageOrigin;
        this.messageVn = messageOrigin;
    }

    ErrorCodeRegisterAutoDebit(String code, String messageOrigin, String messageVn) {
        this.code = code;
        this.messageOrigin = messageOrigin;
        this.messageVn = messageVn;
    }


    /**
     * @return always not null
     */
    public static IEnumCode findByCode(String code, String defaultMessage) {
        try {
            for (IEnumCode el : values()) {
                if (el.getCode().equals(code)) {
                    return el;
                }
            }
            return new EnumCodeUnknown(code, defaultMessage);
        } catch (Exception ex) {
            return new EnumCodeUnknown(code, defaultMessage);
        }
    }
}

