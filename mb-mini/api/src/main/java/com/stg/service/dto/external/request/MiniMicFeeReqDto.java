package com.stg.service.dto.external.request;

import com.stg.utils.Common;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


@Data
@Accessors(chain = true)
public class MiniMicFeeReqDto {
    @NotNull(message = "Ngày sinh bắt buộc nhập")
    @Schema(description = "Ngày sinh khách hàng yyyy-mm-dd", example = "1991-01-01", required = true)
    private String dob;
    @Schema(description = "nhom quyen loi MIC", required = true, example = "1,2,3,4,5")
    @NotNull(message = "Thông tin bắt buộc nhập")
    private int nhom;

    @Valid
    @Schema(description = "Các điều khoản bổ sung để tính phí MIC", required = true)
    @NotNull(message = "Điều khoản bổ sung cần được nhập")
    private Common.GcnMicCareDkbs gcn_miccare_dkbs;

    @Valid
    @Schema(description = "ttin_hd_bme")
    private Common.ParentContract parentContract;

}
