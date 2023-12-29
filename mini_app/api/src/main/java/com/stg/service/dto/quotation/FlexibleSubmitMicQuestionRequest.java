package com.stg.service.dto.quotation;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class FlexibleSubmitMicQuestionRequest {

    @NotNull(message = "Giá trị assuredId bắt buộc nhập")
    @ApiModelProperty(name = "miniAssuredId của người mua bảo hiểm MIC", required = true)
    private String miniAssuredId;

    @ApiModelProperty(notes = "Combo 3 cau hoi va cau hoi khac")
    @Valid
    private MicQuestion micQuestion;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class MicQuestion {
        @ApiModelProperty(notes = "question 1")
        @Pattern(regexp = "C|K", message = "Giá trị là C|K")
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String c1;
        @ApiModelProperty(notes = "question 2")
        @Pattern(regexp = "C|K", message = "Giá trị là C|K")
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String c2;
        @ApiModelProperty(notes = "question 3")
        @Pattern(regexp = "C|K", message = "Giá trị là C|K")
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String c3;

    }

}
