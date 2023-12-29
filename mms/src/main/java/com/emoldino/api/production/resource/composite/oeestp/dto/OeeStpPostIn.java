package com.emoldino.api.production.resource.composite.oeestp.dto;

import com.emoldino.api.common.resource.base.option.dto.RiskLevelConfig;

import lombok.Data;

@Data
public class OeeStpPostIn {
	private RiskLevelConfig oeeScore;
	private RiskLevelConfig oeeMetricAvailability;
	private RiskLevelConfig oeeMetricPerformance;
	private RiskLevelConfig oeeMetricQuality;
}
