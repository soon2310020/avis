package com.stg.service3rd.mbal.dto;


import com.stg.service.dto.mbal.AmountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuotationAmountRespDto {
    private AmountType transactionType;
    private BigDecimal value;
    private Integer startYear;
    private Integer endYear;
}
