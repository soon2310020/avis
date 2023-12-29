package com.emoldino.api.production.resource.composite.oeestp.dto;

import com.emoldino.api.common.resource.base.option.dto.RiskLevelConfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OeeStpGetOut {
	private RiskLevelConfig oeeScore;
	private RiskLevelConfig oeeMetricAvailability;
	private RiskLevelConfig oeeMetricPerformance;
	private RiskLevelConfig oeeMetricQuality;
}
