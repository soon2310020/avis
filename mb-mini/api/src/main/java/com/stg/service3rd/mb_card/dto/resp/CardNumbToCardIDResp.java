package com.stg.service3rd.mb_card.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardNumbToCardIDResp {
    @Schema(name = "ID thẻ", required = true)
    private String cardID;

    @Schema(name = "Số thẻ", required = true)
    private String cardNumber;

    @Schema(name = "ID response", required = true)
    private String responseID;

    @Schema(name = "Thông tin lỗi", required = true)
    private ErrorInfo errorInfo;

    /***/
    @Getter
    @Setter
    public static final class ErrorInfo {
        @Schema(name = "Mã lỗi", required = true)
        private String code;

        @Schema(name = "Error message", required = true)
        private String message;

        @Schema(name = "Error object")
        private String target;

        @Schema(name = "Chi tiết")
        private String details;
    }

}
