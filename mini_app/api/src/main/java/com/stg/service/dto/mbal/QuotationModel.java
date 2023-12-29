package com.stg.service.dto.mbal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class QuotationModel {
	private Long quotationId;
	private Long submissionId;
	private QuotationModelCashFlow cashFlow;
}
