package com.stg.service.dto.external.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
public class MbalTokenReqDto {

    @NotNull(message = "Giá trị master token bắt buộc nhập")
    @Schema(description = "Giá trị master token từ MB app", required = true)
    private String token;

}
