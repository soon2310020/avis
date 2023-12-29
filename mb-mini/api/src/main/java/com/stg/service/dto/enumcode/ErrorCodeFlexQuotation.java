package com.stg.service.dto.enumcode;

import com.stg.service3rd.common.dto.enumcode.EnumCodeUnknown;
import com.stg.service3rd.common.dto.enumcode.IEnumCode;
import lombok.Getter;

import static com.stg.service3rd.common.utils.ApiUtil.SERVER_ERROR_MESSAGE;

@Getter
public enum ErrorCodeFlexQuotation implements IEnumCode {
    CALL_PQM_ERROR("650001", "Lỗi gọi qua bên PQM", SERVER_ERROR_MESSAGE),
    IDENTIFY_INVALID("650010", "Chứng minh nhân dân có 9 hoặc 12 kí tự số", "Thông tin Giấy tờ tùy thân của Quý khách đang chưa hợp lệ. Vui lòng kiểm tra lại"),
    CALL_MBAL_CORE_ERROR("650009", "Lỗi call hệ thống core", SERVER_ERROR_MESSAGE),
    CALL_PQM_CONFIRM_QUOTATION_ERROR("650002", "Lỗi xác nhận Quotation gửi qua PQM", SERVER_ERROR_MESSAGE),
    ;
    private final String code;
    private final String messageOrigin;
    private final String messageVn;

    /***/
    ErrorCodeFlexQuotation(String code, String messageOrigin) {
        this(code, messageOrigin, messageOrigin);
    }

    ErrorCodeFlexQuotation(String code, String messageOrigin, String messageVn) {
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

