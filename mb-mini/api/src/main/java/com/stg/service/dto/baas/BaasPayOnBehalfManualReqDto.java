package com.stg.service.dto.baas;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@Accessors(chain = true)
public class BaasPayOnBehalfManualReqDto {

    @NotBlank(message = "ID giao dịch không được blank")
    @Schema(description = "Id giao dịch MB", example = "AW112WUD5XQ6", required = true)
    private String mbTransactionId;

    @Pattern(regexp = "MIC|MBAL", message = "Lựa chọn chi hộ cho MBAL hoặc MIC")
    @NotBlank(message = "Đối tượng chi hộ không được phép empty")
    @Schema(description = "Đối tượng chi hộ MBAL/MIC", example = "MIC", required = true)
    private String type;
}
