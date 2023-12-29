package com.stg.service.dto.external.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class MbCreateMerchantTokenReqDto {
    @NotNull
    @Schema(description = "masterSessionToken", required = true)
    private String masterSessionToken;

    @NotNull
    @Schema(description = "MBAL merchantCode", required = true)
    private String merchantCode;

}
