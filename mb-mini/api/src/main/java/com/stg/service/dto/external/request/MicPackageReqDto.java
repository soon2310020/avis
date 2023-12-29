package com.stg.service.dto.external.request;


import com.stg.utils.Common;
import com.stg.utils.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class MicPackageReqDto {

    @NotNull(message = "Ngày sinh bắt buộc nhập")
    @Schema(description = "Ngày sinh khách hàng yyyy-mm-dd", example = "1991-01-01", required = true)
    private String dob;

    @Schema(description = "ttin_hd_bme")
    @Valid
    private Common.ParentContract parentContract;

    @Schema(description = "Giới tính FEMALE, MALE", example = "FEMALE", required = true)
    private Gender gender;

    @Schema(description = "Thông tin bố mẹ")
    @Valid
    private MicParentReqDto parentInfo;

}
