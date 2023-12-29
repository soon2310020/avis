package com.stg.service.dto.baas;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Accessors(chain = true)
public class InsurancePaymentRetryReq {

    @NotBlank(message = "ID giao dịch không được blank")
    @Schema(description = "Id giao dịch MB", example = "AW112WUD5XQ6", required = true)
    private String mbTransactionId;

}
