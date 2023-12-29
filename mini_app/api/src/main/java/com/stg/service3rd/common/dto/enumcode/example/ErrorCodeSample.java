package com.stg.service3rd.common.dto.enumcode.example;

import com.stg.service3rd.common.dto.enumcode.EnumCodeUnknown;
import com.stg.service3rd.common.dto.enumcode.IEnumCode;
import lombok.Getter;

@Getter
public enum ErrorCodeSample implements IEnumCode {
    OK("000", "OK", "Thành công"),
    TIME_OUT("002", "Time out", "Quá thời hạn chờ của máy chủ"),
    UNKNOWN_ERROR("500", "Unknown error", "Lỗi không xác định"),
    REGISTER_NUMBER_INVALID("20033", "Registration number invalid", "Mã đăng ký kinh doanh không hợp lệ"),
    SOURCE_TYPE_INVALID("20034", "Source type invalid", "Loại tài khoản không hợp lệ"),
    SOURCE_NUMBER_INVALID("20035", "Source number invalid", "Số tài khoản không hợp lệ"),
    SOURCE_NAME_INVALID("20036", "Source name invalid", "Tên tài khoản không hợp lệ"),
    EMAIL_INVALID("200138", "Email input invalid", "Địa chỉ email không hợp lệ"),
    CURRENCY_INVALID("202", "Currency is invalid", "Đơn vị tiền tệ không hợp lệ"),
    ;
    private final String code;
    private final String messageOrigin;
    private final String messageVn;

    /***/
    ErrorCodeSample(String code, String messageOrigin) {
        this.code = code;
        this.messageOrigin = messageOrigin;
        this.messageVn = messageOrigin;
    }

    ErrorCodeSample(String code, String messageOrigin, String messageVn) {
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

