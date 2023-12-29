package com.stg.service.dto.external.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@Accessors(chain = true)
public class MbalIllustrationBoardReqDto {

    private String packageCode;
    @Pattern(regexp = "MALE|FEMALE", message = "Gender must be MALE or FEMALE")
    private String gender;
    @Schema(description = "Ngày sinh", example = "1991-12-20", required = true)
    private String age;
    private Integer occupationClass;
    private String packageType;
    @NotNull(message = "deathBenefit must not be null")
    private String deathBenefit;
    @Schema(description = "Phí bảo hiểm cơ bản MBAL", example = "6000000", required = true)
    @NotNull(message = "insuranceFee must not be null")
    private Long insuranceFee;

}
