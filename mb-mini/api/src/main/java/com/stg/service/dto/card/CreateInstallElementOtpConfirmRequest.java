package com.stg.service.dto.card;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInstallElementOtpConfirmRequest {
    @Schema(description = "Request Id")
    @Size(max = 50, min = 1)
    private String requestId;
    @Schema(description = "Mã OTP")
    @Size(max = 20, min = 1)
    private String otp;
    private String signature;
}
