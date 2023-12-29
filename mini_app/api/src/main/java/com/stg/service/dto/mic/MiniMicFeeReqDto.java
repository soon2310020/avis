package com.stg.service.dto.mic;

import com.stg.service3rd.mbal.dto.FlexibleCommon;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Accessors(chain = true)
public class MiniMicFeeReqDto {
    @NotNull(message = "Ngày sinh bắt buộc nhập")
    @ApiModelProperty(notes = "Ngày sinh khách hàng yyyy-mm-dd", example = "1991-01-01", required = true)
    private String dob;
    @ApiModelProperty(notes = "nhom quyen loi MIC", required = true, example = "1,2,3,4,5")
    @NotNull(message = "Thông tin bắt buộc nhập")
    private int nhom;

    @Valid
    @ApiModelProperty(notes = "Các điều khoản bổ sung để tính phí MIC", required = true)
    @NotNull(message = "Điều khoản bổ sung cần được nhập")
    private FlexibleCommon.GcnMicCareDkbs gcn_miccare_dkbs;

    @Valid
    @ApiModelProperty(notes = "ttin_hd_bme")
    private FlexibleCommon.ParentContract parentContract;
}
