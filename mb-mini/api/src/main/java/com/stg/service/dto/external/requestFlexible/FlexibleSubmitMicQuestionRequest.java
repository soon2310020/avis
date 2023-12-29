package com.stg.service.dto.external.requestFlexible;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(name = "miniAssuredId của người mua bảo hiểm MIC", required = true)
    private String miniAssuredId;

    @Schema(description = "Combo 3 cau hoi va cau hoi khac")
    @Valid
    private MicQuestion micQuestion;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    public static class MicQuestion {
        @Schema(description = "question 1")
        @Pattern(regexp = "C|K", message = "Giá trị là C|K")
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String c1;
        @Schema(description = "question 2")
        @Pattern(regexp = "C|K", message = "Giá trị là C|K")
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String c2;
        @Schema(description = "question 3")
        @Pattern(regexp = "C|K", message = "Giá trị là C|K")
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String c3;

    }

}
