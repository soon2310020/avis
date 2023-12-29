package com.stg.service.dto.quotation;

import com.stg.service3rd.mbal.dto.FlexibleCommon;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UpdateQuoteAndGenQRCodeReq {

    @NotNull(message = "Giá trị processId bắt buộc nhập")
    @ApiModelProperty(name = "Id tiến trình mua bảo hiểm", required = true)
    private long processId;

    @ApiModelProperty(notes = "Danh sách câu hỏi trả lời cho những người được bảo hiểm", required = true)
    @NotNull(message = "Thông tin bắt buộc nhập")
    private List<FlexibleSubmitQuestionInput> healths;

    @NotNull(message = "Thông tin sale bắt buộc nhập")
    @ApiModelProperty(name = "Thông tin IC")
    private FlexibleCommon.Sale sale; /*flex-e-app*/

    @NotNull(message = "Thông tin sale bắt buộc nhập")
    @ApiModelProperty(name = "Thông tin supporter")
    private FlexibleCommon.ReferrerInput supporter; /*flex-e-app*/

    @Valid
    @ApiModelProperty(name = "Thông tin referrer")
    private FlexibleCommon.ReferrerInput referrer; /*flex-e-app*/

    @ApiModelProperty(notes = "Danh sách câu hỏi trả lời cho những người được bảo hiểm MIC", required = true)
    @Valid
    private List<FlexibleSubmitMicQuestionRequest> micHealths;
}
