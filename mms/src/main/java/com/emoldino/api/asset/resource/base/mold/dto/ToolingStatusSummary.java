package com.emoldino.api.asset.resource.base.mold.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolingStatusSummary {
	private long inProduction;
	private long idle;

	private long inactive;
	private long onStandby;

	private long sensorOffline;
	private long sensorDetached;
	private long noSensor;
}
