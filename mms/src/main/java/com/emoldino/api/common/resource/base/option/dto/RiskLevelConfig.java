package com.emoldino.api.common.resource.base.option.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskLevelConfig {
	private int low;
	private int medium;
	private int high;
}
