package com.stg.service.dto.external.requestFlexible;


import com.stg.service.dto.validator.Less;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Less(field = "startYear", dependField = "endYear", message = "startYear must be less than endYear")
public class QuotationAmountDto {

    @NotNull
    private BigDecimal value;

    private String transactionType;

    @NotNull
    private Integer startYear;

    @NotNull
    private Integer endYear;

}
