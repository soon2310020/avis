package com.stg.service.dto.external.requestFlexible;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import static com.stg.utils.Constants.EMAIL_PATTERN;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SendMailFlexibleReqDto {
    @NotNull(message = "Giá trị bắt buộc nhập")
    @Email
    @Pattern(regexp = EMAIL_PATTERN)
    private String recipient;
    @NotNull(message = "Giá trị processId bắt buộc nhập")
    @Schema(name = "Id tiến trình mua bảo hiểm", required = true)
    private Integer processId;

}
