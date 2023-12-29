package com.stg.service.dto.baas;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@Data
@Accessors(chain = true)
public class ManualRequest {

    @NotBlank(message = "ID giao dịch không được blank")
    @Schema(description = "Id giao dịch MB", example = "AW112WUD5XQ6", required = true)
    private String mbTransactionId;

}
