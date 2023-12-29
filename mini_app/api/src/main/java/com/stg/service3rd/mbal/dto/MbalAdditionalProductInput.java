package com.stg.service3rd.mbal.dto;

import com.stg.service3rd.mbal.constant.ProductType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel
@Valid
public class MbalAdditionalProductInput {
    @ApiModelProperty(required = true)
    @NotNull(message = "Thông tin bắt buộc nhập")
    private ProductType productType;
    @ApiModelProperty(required = true)
    @NotNull(message = "Thông tin bắt buộc nhập")
    private Integer policyTerm;
    @NotNull(message = "Thông tin bắt buộc nhập")
    @ApiModelProperty(required = true)
    private Integer premiumTerm;
    @NotNull(message = "Thông tin bắt buộc nhập")
    @ApiModelProperty(required = true)
    private BigDecimal sumAssured;
    @ApiModelProperty(notes = "Không bắt buộc nhập")
    private Integer assuredIndex;
//    @ApiModelProperty(notes = "Giá trị bắt buộc nếu Rider là HSCR")
//    private BigDecimal annualIncome;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MbalAdditionalProductInput that = (MbalAdditionalProductInput) o;
        return productType == that.productType && Objects.equals(assuredIndex, that.assuredIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productType, assuredIndex);
    }
}
