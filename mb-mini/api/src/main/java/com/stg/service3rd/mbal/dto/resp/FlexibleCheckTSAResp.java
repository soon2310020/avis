package com.stg.service3rd.mbal.dto.resp;

import com.stg.utils.IdentificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
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
        @Schema(required = true, description = "Loại giấy tờ tùy thân")
        @NotNull(message = "Thông tin bắt buộc nhập")
        private IdentificationType identificationType;

        @Schema
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String identificationNumber;

        private Integer appQuestionNumber;
    }
}
