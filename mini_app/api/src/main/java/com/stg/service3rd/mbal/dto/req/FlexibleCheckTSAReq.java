package com.stg.service3rd.mbal.dto.req;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.stg.service.dto.quotation.QuotationAmountDto;
import com.stg.service.dto.quotation.QuotationAssuredDto;
import com.stg.service.dto.quotation.QuotationHeaderDto;
import com.stg.service.dto.quotation.QuotationProductDto;
import com.stg.service3rd.mbal.constant.AssuredType;
import com.stg.service3rd.mbal.constant.ProductType;
import com.stg.service3rd.mbal.dto.FlexibleCommon;
import com.stg.service3rd.mbal.dto.MbalAdditionalProductInput;
import com.stg.utils.DateUtil;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

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

    @Valid
    private List<QuotationAmountDto> amounts;

    @ApiModelProperty
    private boolean raiderDeductFund;

    public FlexibleCheckTSAReq(QuotationHeaderDto header) {
        setCustomer(header.getCustomer());

        QuotationAssuredDto assured = header.isCustomerIsAssured() ? header.getCustomer() : header.getAssured();
        List<FlexibleCommon.Assured> assureds = new ArrayList<>();
        assureds.add(map(assured, AssuredType.LIFE_ASSURED));
        if (header.getAdditionalAssureds() != null) {
            assureds.addAll(header.getAdditionalAssureds().stream().map(a -> map(a, AssuredType.ADDITIONAL_LIVES))
                    .collect(Collectors.toList()));
        }
        setAssureds(assureds);

        if (header.getBeneficiary() != null) {
            setBeneficiaries(List.of(header.getBeneficiary()));
        }

        if (assured.getAdditionalProducts() != null) {
            setAdditionalProducts(
                    assured.getAdditionalProducts().stream().map(p -> map(p)).collect(Collectors.toList()));
        }

        setProductPackage(new FlexibleCommon.ProductPackageInput(header));

        if (header.getAmount() != null) {
            setAmounts(List.of(header.getAmount()));
        }

        setRaiderDeductFund(header.isRaiderDeductFund());
    }

    public void setCustomer(QuotationAssuredDto dto) {
        customer = new FlexibleCommon.CustomerInfo(map(dto, null));
    }

    private FlexibleCommon.Assured map(QuotationAssuredDto dto, AssuredType type) {
        FlexibleCommon.Assured assured = new FlexibleCommon.Assured();
        assured.setType(type);
        assured.setFullName(dto.getFullName());
        assured.setAddress(dto.getAddress());
        assured.setDob(DateUtil.localDateTimeToString(dto.getDob()));
        assured.setGender(dto.getGender());
        assured.setEmail(dto.getEmail());
        assured.setOccupationId(dto.getOccupationId());
        assured.setPhoneNumber(dto.getPhoneNumber());
        assured.setNationalityCode(dto.getNationalityCode());
        assured.setIdentificationType(dto.getIdentificationType());
        assured.setIdentificationId(dto.getIdentificationId());
        assured.setIdentificationDate(dto.getIdentificationDate());
        assured.setIdIssuedPlace(dto.getIdIssuedPlace());
        assured.setAnnualIncome(dto.getAnnualIncome());
        assured.setAddress(dto.getAddress());
        assured.setOccupationId(dto.getOccupationId());
        return assured;
    }

    private MbalAdditionalProductInput map(QuotationProductDto dto) {
        return new MbalAdditionalProductInput(ProductType.valueOf(dto.getProductType().name()), dto.getPolicyTerm(),
                dto.getPremiumTerm(), dto.getSumAssured(), 0);
    }

}
