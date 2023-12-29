package com.stg.service.dto.card;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonCardResponse {
    private String clientMessageId;
    @Schema(description = "Mã lỗi")
    private String errorCode;
    @Schema(description = "Mô tả lỗi")
    private String errorDescription;
    private Object data;
    private String signature;
}
