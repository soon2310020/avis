package com.emoldino.api.integration.resource.composite.inftol.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.SizeUnit;
import saleson.common.enumeration.WeightUnit;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfTolPostItem {
	private String toolingType;

	private String toolDescription;

	private Long toolSizeWidth;
	private Long toolSizeLength;
	private Long toolSizeHeight;
	private SizeUnit toolSizeUnit;

	private Long toolWeight;
	private WeightUnit toolWeightUnit;

	private Long warrantyShotCount;
}
