package com.stg.service.dto.external.requestFlexible;

import com.stg.utils.FlexibleCommon;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ProcessFlexibleReqDto {

    @Valid
    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(name = "Thông tin người mua bảo hiểm", required = true)
    private FlexibleCommon.CustomerInfo customer;

    @Schema(name = "Id gói bảo hiểm", required = true)
    private Integer productPackageId;

    @Schema(name = "Mã code gói bảo hiểm")
    private String productPackageCode;

    @Schema(name = "Người mua bảo hiểm cũng là người hưởng bảo hiểm chính", required = true)
    @NotNull(message = "Thông tin bắt buộc nhập")
    private boolean customerIsAssured = true;

    @Schema(name = "UUID của quotation trên crm-tool")
    private UUID toolUid;
}
