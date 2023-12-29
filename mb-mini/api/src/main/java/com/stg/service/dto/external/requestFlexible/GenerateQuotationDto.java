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
public class GenerateQuotationDto {

    @NotNull(message = "Giá trị processId bắt buộc nhập")
    @Schema(name = "Id tiến trình mua bảo hiểm", required = true)
    private Integer processId;

    @NotNull(message = "Thông tin sale bắt buộc nhập")
    @Schema(name = "Thông tin IC")
    private FlexibleCommon.Sale sale;

    @Schema(name = "Thông tin supporter")
    private FlexibleCommon.RefererInput supporter;

    @Valid
    @Schema(name = "Thông tin referrer")
    private FlexibleCommon.RefererInput referer;

    @Schema(name = "Thông tin NĐBH và NĐBHBS, BMBH cùng với các gói bảo hiểm bổ sung đi kèm")
    @Valid
    private List<QuoteAssuredInput> assuredInputs;

    @Schema(required = true, description = "Gói bảo hiểm chính")
    @Valid
    @NotNull(message = "Gói bảo hiểm chính bắt buộc lựa chọn")
    private FlexibleCommon.ProductPackageInput productPackage;

    @Schema
    private boolean raiderDeductFund;

    @Valid
    @Schema(description = "Phí đóng thêm")
    private QuotationAmountDto amount;

    @Schema(description = "Người thụ hưởng")
    @Valid
    private FlexibleCommon.BeneficiaryInput beneficiary;

//    @Schema(description = "Giá trị bắt buộc nhập cho MainAssured")
//    @NotNull(message = "Gia tri bắt buộc lựa chọn")
//    private BigDecimal annualIncome;

}
