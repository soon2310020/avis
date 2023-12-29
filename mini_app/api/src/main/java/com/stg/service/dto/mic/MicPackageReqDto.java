package com.stg.service.dto.mic;

import com.stg.constant.Gender;
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
public class MicPackageReqDto {

    @NotNull(message = "Ngày sinh bắt buộc nhập")
    @ApiModelProperty(notes = "Ngày sinh khách hàng yyyy-mm-dd", example = "1991-01-01", required = true)
    private String dob;

    @ApiModelProperty(notes = "ttin_hd_bme")
    @Valid
    private FlexibleCommon.ParentContract parentContract;

    @ApiModelProperty(notes = "Giới tính FEMALE, MALE", example = "FEMALE", required = true)
    private Gender gender;

    @ApiModelProperty(notes = "Thông tin bố mẹ")
    @Valid
    private MicParentReqDto parentInfo;
}
