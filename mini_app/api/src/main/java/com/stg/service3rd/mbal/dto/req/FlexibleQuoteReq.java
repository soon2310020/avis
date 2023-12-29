package com.stg.service3rd.mbal.dto.req;

import com.stg.service.dto.quotation.QuotationAmountDto;
import com.stg.service3rd.mbal.dto.FlexibleCommon;
import com.stg.service3rd.mbal.dto.MbalAdditionalProductInput;
import com.stg.service3rd.mbal.validator.MbalDateTimeFormat;
import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(name = "Id tiến trình mua bảo hiểm", required = true)
    private Long processId;

    @ApiModelProperty(name = "Thông tin IC")
    private FlexibleCommon.Sale sale;

    @ApiModelProperty(name = "Thông tin supporter")
    private FlexibleCommon.Referrer3rdInput supporter;

    @Valid
    @ApiModelProperty(name = "Thông tin referrer")
    private FlexibleCommon.Referrer3rdInput referrer;

    @MbalDateTimeFormat
    @ApiModelProperty(name = "Ngày tạo quote", example = "format: yyyy-MM-dd")
    private String quotationDate;

    @Valid
    @ApiModelProperty(name = "Thông tin người mua bảo hiểm", required = true)
    private FlexibleCommon.CustomerInfo customer;

    @ApiModelProperty(notes = "Danh sách người hưởng bảo hiểm chính và bổ sung")
    @Valid
    private List<FlexibleCommon.Assured> assureds;

    @ApiModelProperty(notes = "Thông tin người thụ hưởng bảo hiểm")
    @Valid
    private List<FlexibleCommon.BeneficiaryInput> beneficiaries;

    @ApiModelProperty(notes = "Thông tin danh sách gói bổ sung")
    @Valid
    private List<MbalAdditionalProductInput> additionalProducts;

    @ApiModelProperty(required = true, notes = "Gói bảo hiểm chính")
    @Valid
    @NotNull(message = "Gói bảo hiểm chính bắt buộc lựa chọn")
    private FlexibleCommon.ProductPackageInput productPackage;

    @ApiModelProperty
    private boolean raiderDeductFund;

    @Valid
    private List<QuotationAmountDto> amounts;

    //@ApiModelProperty(notes = "Giá trị bắt buộc nếu Rider là HSCR")
    private BigDecimal annualIncome;

}
