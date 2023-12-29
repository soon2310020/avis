package com.stg.service3rd.mb_card.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class CardNumbToCardIDReq {

    @NotNull(message = "ID yêu cầu bắt buộc nhập")
    @Schema(description = "ID yêu cầu (40)", required = true)
    private String requestID;

    @NotNull(message = "Số thẻ bắt buộc nhập")
    @Schema(description = "Số thẻ (19)", required = true)
    private String cardNumber;
}

