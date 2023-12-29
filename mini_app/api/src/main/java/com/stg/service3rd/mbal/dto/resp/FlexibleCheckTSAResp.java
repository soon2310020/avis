package com.stg.service3rd.mbal.dto.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

import com.stg.constant.IdentificationType;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FlexibleCheckTSAResp {
    private List<Assured> assureds;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Assured {
        @ApiModelProperty(required = true, notes = "Loại giấy tờ tùy thân")
        @NotNull(message = "Thông tin bắt buộc nhập")
        private IdentificationType identificationType;

        @ApiModelProperty
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String identificationNumber;

        private Integer appQuestionNumber;
    }
}
