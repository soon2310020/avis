package com.stg.service.dto.external.requestFlexible;

import com.stg.utils.ProductType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema
@Valid
public class MbalAdditionalProductInput {
    @Schema(required = true)
    @NotNull(message = "Thông tin bắt buộc nhập")
    private ProductType productType;
    @Schema(required = true)
    @NotNull(message = "Thông tin bắt buộc nhập")
    private Integer policyTerm;
    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(required = true)
    private Integer premiumTerm;
    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(required = true)
    private BigDecimal sumAssured;
    @Schema(description = "Không bắt buộc nhập")
    private Integer assuredIndex;
//    @Schema(description = "Giá trị bắt buộc nếu Rider là HSCR")
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
