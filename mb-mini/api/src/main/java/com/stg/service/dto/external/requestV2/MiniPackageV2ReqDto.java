package com.stg.service.dto.external.requestV2;

import com.stg.service.dto.validator.MbalDateTimeFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;


@Data
@Accessors(chain = true)
public class MiniPackageV2ReqDto {
    @NotNull(message = "Thông tin bắt buộc nhập")
    @MbalDateTimeFormat
    @Schema(description = "Ngày sinh customer, format: yyyy-mm-dd", example = "1991-09-01", required = true)
    private String dob;
    @Pattern(regexp = "HAPPY|HEALTHY", message = "insurancePackageName must be HAPPY or HEALTHY")
    @Schema(description = "Tên gói bảo hiểm", example = "HAPPY", required = true)
    @NotNull(message = "Tên gói bảo hiểm bắt buộc nhập")
    private String insurancePackageName;

    @Schema(description = "Loại gói bảo hiểm", example = "FIX_COMBO", required = true)
    @Pattern(regexp = "FIX_COMBO|FREE_STYLE", message = "type must be FIX_COMBO or FREE_STYLE")
    private String type;

}
