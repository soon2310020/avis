package com.stg.service3rd.toolcrm.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Setter
public class QuotationDetailReq {

    @NotNull(message = "Giá trị quotation uuid bắt buộc nhập")
    @Schema(description = "Giá trị quotation uuid", required = true)
    private UUID quotationUid;

    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "Giá trị CMT/CCCD", required = true)
    private String identificationNumber;

    @NotNull(message = "Thông tin bắt buộc nhập")
    //@Pattern(regexp = "CMND|CCCD|CMTQD", message = "CMND | CCCD | CMTQD")
    @Schema(description = "Loại CMT/CCCD", required = true)
    private String identificationType;
}
