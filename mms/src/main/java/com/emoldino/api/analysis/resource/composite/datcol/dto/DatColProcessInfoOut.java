package com.emoldino.api.analysis.resource.composite.datcol.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatColProcessInfoOut {
	private String counterId;
	private int week;
	private String measuredTime;
	private Double injectionTime;
	private Double packingTime;
	private Double coolingTime;
	private Double injectionPressureIndex;
	private Double packingPressureIndex;
}
