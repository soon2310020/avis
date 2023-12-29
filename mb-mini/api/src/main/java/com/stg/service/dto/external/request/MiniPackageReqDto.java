package com.stg.service.dto.external.request;

import com.stg.service.dto.validator.MbalDateTimeFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;


@Data
@Accessors(chain = true)
public class MiniPackageReqDto {
    // Data for Mic
    @NotNull(message = "Thông tin bắt buộc nhập")
    @MbalDateTimeFormat
    @Schema(description = "Ngày sinh customer, format: yyyy-mm-dd", example = "1991-09-01", required = true)
    private String dob;
    @Pattern(regexp = "CUSTOM|HAPPY|HEALTHY", message = "type must be CUSTOM or HAPPY or HEALTHY")
    @Schema(description = "Loại gói bảo hiểm", example = "HAPPY", required = true)
    private String type;

}
