package com.emoldino.api.asset.resource.composite.wgttoltco.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WgtTolTcoGetOut {
	private long acquisitionCost;
	private long maintenanceCost;
	private String currencyCode;
	private String currencySymbol;

	public long getTco() {
		return acquisitionCost + maintenanceCost;
	}
}
