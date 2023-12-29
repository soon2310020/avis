package com.stg.service.dto.card;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CreateInstallElementNoOtpRequest {
    @Schema(description = "Kỳ hạn CĐTG")
    @Size(min = 1, max = 3)
    private String period;
    @Schema(description = "Mã GD muốn chuyển đổi trả góp")
    @Size(min = 1, max = 15)
    private String retRefNumber;
    @Size(min = 1, max = 15)
    @Schema(description = "Đơn vị chấp nhận thanh toán")
    private String merchant;
}
