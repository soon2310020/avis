package com.stg.service3rd.toolcrm.dto.resp.quote;

import com.stg.service.dto.external.requestFlexible.MbalAdditionalProductInput;
import com.stg.service.dto.external.requestFlexible.QuotationAmountDto;
import com.stg.service.dto.validator.MbalDateTimeFormat;
import com.stg.utils.FlexibleCommon;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Slf4j
public class FlexibleQuoteReq {

    @NotNull(message = "Giá trị processId bắt buộc nhập")
    @Schema(name = "Id tiến trình mua bảo hiểm", required = true)
    private Long processId;

    @Schema(name = "Thông tin IC")
    private FlexibleCommon.Sale sale;

    @Schema(name = "Thông tin supporter")
    private FlexibleCommon.Referrer3rdInput supporter;

    @Valid
    @Schema(name = "Thông tin referrer")
    private FlexibleCommon.Referrer3rdInput referrer;

    @MbalDateTimeFormat
    @Schema(name = "Ngày tạo quote", example = "format: yyyy-MM-dd")
    private String quotationDate;

    @Valid
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

    @Schema
    private boolean raiderDeductFund;

    @Valid
    private List<QuotationAmountDto> amounts;

    //@Schema(description = "Giá trị bắt buộc nếu Rider là HSCR")
    private BigDecimal annualIncome;

}
