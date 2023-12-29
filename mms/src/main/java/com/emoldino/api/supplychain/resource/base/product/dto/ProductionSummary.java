package com.emoldino.api.supplychain.resource.base.product.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.PriorityType;

@Data
@NoArgsConstructor
public class ProductionSummary {
	private long demand;
	private long produced;
	private long predicted;
	private long capacity;
	private double demandComplianceRate;
	private PriorityType demandCompliance;
}
