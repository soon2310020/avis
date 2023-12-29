package com.stg.service.dto.external.responseFlexible;


import com.stg.service.dto.validator.Less;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuotationAmountRespDto {

    private BigDecimal value;

    private String transactionType;

    private Integer startYear;

    private Integer endYear;

}
