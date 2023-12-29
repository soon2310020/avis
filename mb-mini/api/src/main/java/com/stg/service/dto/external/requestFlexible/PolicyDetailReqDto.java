package com.stg.service.dto.external.requestFlexible;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PolicyDetailReqDto {

    @Schema(name = "Mã số AF.XXX bảo hiểm")
    private String appNumber;

    @Schema(name = "Số hợp đồng bảo hiểm")
    private String policyNumber;

}
