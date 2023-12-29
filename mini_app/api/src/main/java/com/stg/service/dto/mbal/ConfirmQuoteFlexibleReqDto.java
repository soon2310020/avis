package com.stg.service.dto.mbal;

import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(name = "Id tiến trình mua bảo hiểm", required = true)
    private Long processId;

}
