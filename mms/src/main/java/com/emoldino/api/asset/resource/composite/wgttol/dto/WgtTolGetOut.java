package com.emoldino.api.asset.resource.composite.wgttol.dto;

import lombok.Data;

@Data
public class WgtTolGetOut {
	private long inProduction;
	private long idle;
	private long inactive;
	private long onStandby;
	private long sensorOffline;
	private long sensorDetached;

	private Long noSensor;

	public long getTotal() {
		return inProduction + idle + inactive + onStandby + sensorOffline + sensorDetached + noSensor;
	}

	public long getDigitalized() {
		return inProduction + idle + inactive + onStandby + sensorOffline + sensorDetached;
	}

	public long getNonDigitalized() {
		return noSensor;
	}
}
