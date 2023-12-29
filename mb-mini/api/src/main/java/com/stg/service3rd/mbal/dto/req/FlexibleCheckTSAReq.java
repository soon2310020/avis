package com.stg.service3rd.mbal.dto.req;

import com.stg.service.dto.external.requestFlexible.MbalAdditionalProductInput;
import com.stg.service.dto.external.requestFlexible.QuotationAmountDto;
import com.stg.utils.FlexibleCommon;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * quotation ULRP 3.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class FlexibleCheckTSAReq {
    @Valid
    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(name = "Thông tin người mua bảo hiểm", required = true)
    private FlexibleCommon.CustomerInfo customer;

    @Schema(description = "Danh sách người hưởng bảo hiểm chính và bổ sung")
    @Valid
    private List<FlexibleCommon.Assured> assureds;

    @Schema(description = "Thông tin người thụ hưởng bảo hiểm")
    @Valid
    private List<FlexibleCommon.BeneficiaryInput> beneficiaries;

    @Schema(description = "Thông tin danh sách gói bổ sung")
    @Valid
    private List<MbalAdditionalProductInput> additionalProducts;

    @Schema(required = true, description = "Gói bảo hiểm chính")
    @Valid
    @NotNull(message = "Gói bảo hiểm chính bắt buộc lựa chọn")
    private FlexibleCommon.ProductPackageInput productPackage;

    @Valid
    private List<QuotationAmountDto> amounts;

    @Schema
    private boolean raiderDeductFund;
}
