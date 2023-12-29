package com.stg.service.dto.external.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
public class TokenReqDto {

    @NotNull(message = "Giá trị token từ PaymentHub bắt buộc nhập")
    @Schema(description = "Giá trị token từ MB app", required = true)
    private String token;

//    @NotNull(message = "Grant type must be required")
//    @Pattern(regexp = "refresh_token|token")
//    @Schema(description = "Giá trị bắt buộc nhập để xác định cách gen accessToken", required = true)
//    private String grantType;
//
//    @Schema(description = "Giá trị bắt buộc khi muốn gen access_token")
//    private String refreshToken;

}
