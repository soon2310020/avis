package com.stg.service.dto.external.requestFlexible;

import com.stg.utils.FlexibleCommon;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SubmitFlexibleReqDto {

    @Schema(name = "Thông tin IC, không cần truyền từ FE")
    private FlexibleCommon.Sale sale;

    @Schema(name = "Thông tin supporter, không cần truyền từ FE")
    private FlexibleCommon.Referrer3rdInput supporter;

    @Valid
    @Schema(name = "Thông tin referrer, không cần truyền từ FE")
    private FlexibleCommon.Referrer3rdInput referrer;

    @Schema
    private FlexibleCommon.SubmitCustomer customer;

    @Valid
    private List<HealthInfoRequest> assureds;

}
