package com.stg.service.dto.external.requestFlexible;

import com.stg.utils.FlexibleCommon;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SubmitQuotationDto {

    @NotNull(message = "Giá trị processId bắt buộc nhập")
    @Schema(name = "Id tiến trình mua bảo hiểm", required = true)
    private Integer processId;

    @Schema(required = true)
    @NotNull(message = "Thông tin bắt buộc nhập")
    private FlexibleCommon.SubmitCustomer customer;

    @Schema(description = "Danh sách câu hỏi trả lời cho những người được bảo hiểm", required = true)
    @NotNull(message = "Thông tin bắt buộc nhập")
    private List<FlexibleSubmitQuestionRequest> healths;

    @Schema(description = "Danh sách câu hỏi trả lời cho những người được bảo hiểm MIC", required = true)
    @Valid
    private List<FlexibleSubmitMicQuestionRequest> micHealths;
}
