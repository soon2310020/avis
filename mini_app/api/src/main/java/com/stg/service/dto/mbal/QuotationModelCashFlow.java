package com.stg.service.dto.mbal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class QuotationModelCashFlow {
	private List<QuotationModelYearItem> yearItems;
}
