package com.stg.service.dto.mic;

import com.stg.service3rd.mbal.dto.MicAdditionalProduct;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import static com.stg.service3rd.mbal.constant.Common.PHONE_PATTERN;


@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel
@Valid
public class MicParentReqDto {

    @ApiModelProperty
    @NotNull(message = "Thông tin bắt buộc nhập")
    private String fullName;

    @ApiModelProperty
    @NotNull(message = "Thông tin bắt buộc nhập")
    private String identificationNumber;

    @NotNull(message = "Thông tin bắt buộc nhập")
    @Pattern(regexp = PHONE_PATTERN, message = "Không đúng định dạng số điện thoại di động Việt Nam")
    private String phoneNumber;

    @NotNull(message = "Thông tin bắt buộc nhập")
    @ApiModelProperty
    @Valid
    private MicAdditionalProduct micRequest;
}
