package com.stg.service.dto.external.requestV2;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
public class MbalIllustrationBoardV2ReqDto {

    private String packageCode;
    private String gender;
    @Schema(description = "Ngày sinh", example = "1991-12-20", required = true)
    private String age;
    private Integer occupationClass;
}
