package com.stg.service.dto.external.requestFlexible;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ConfirmQuoteFlexibleReqDto {
    @NotNull(message = "Giá trị processId bắt buộc nhập")
    @Schema(name = "Id tiến trình mua bảo hiểm", required = true)
    private Integer processId;

}
