package com.emoldino.api.asset.resource.composite.wgttolopr.dto;

import lombok.Data;

@Data
public class WgtTolOprGetOut {
	private long inProduction;
	private long idle;

	private long inactive;
	private long onStandby;

	private long sensorOffline;
	private long sensorDetached;
	private long noSensor;

	public long getTotal() {
		return inProduction + idle + inactive + onStandby + sensorOffline + sensorDetached + noSensor;
	}

	public long getInOperation() {
		return inProduction + idle;
	}

	public long getOutOfOperation() {
		return inactive + onStandby;
	}

	public long getUnknown() {
		return sensorOffline + sensorDetached + noSensor;
	}
}
