package com.stg.service.dto.quotation;

import com.stg.service.dto.mbal.AmountType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//@Less(field = "startYear", dependField = "endYear", message = "start_year must be less than end_year")
public class QuotationAmountDto {
    private AmountType transactionType = AmountType.TOP_UP;

    private BigDecimal value;
    private Integer startYear;
    private Integer endYear;
}
