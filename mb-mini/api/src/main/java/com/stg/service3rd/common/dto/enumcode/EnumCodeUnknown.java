package com.stg.service3rd.common.dto.enumcode;

import lombok.Getter;
import org.springframework.lang.NonNull;

@Getter
public class EnumCodeUnknown implements IEnumCode {
    private final String code;
    private final String messageOrigin;
    private final String messageVn;

    /***/
    public EnumCodeUnknown(@NonNull String code, String messageOrigin) {
        this.code = code;
        this.messageOrigin = messageOrigin == null ? "Not found error code" : messageOrigin;
        this.messageVn = messageOrigin == null ? "Không thành công" : messageOrigin;
    }

    public EnumCodeUnknown(@NonNull String code, String messageOrigin, String messageVn) {
        this.code = code;
        this.messageOrigin = messageOrigin == null ? "Not found error code" : messageOrigin;
        this.messageVn = messageVn == null ? "Không thành công" : messageVn;
    }
}

