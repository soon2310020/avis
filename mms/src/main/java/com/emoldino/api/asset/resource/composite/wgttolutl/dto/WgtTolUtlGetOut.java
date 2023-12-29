package com.emoldino.api.asset.resource.composite.wgttolutl.dto;

import java.math.BigDecimal;

import com.emoldino.api.asset.resource.base.mold.dto.ToolingStatusSummary;
import com.emoldino.api.common.resource.base.option.dto.UtilizationConfig;
import com.emoldino.framework.util.ValueUtils;

import lombok.Data;

@Data
public class WgtTolUtlGetOut {
	private String lowStd;
	private long low;
	private String mediumStd;
	private long medium;
	private String highStd;
	private long high;
	private String prolongedStd;
	private long prolonged;

	private long inProduction;
	private long idle;
	private long inactive;
	private long onStandby;
	private long sensorOffline;
	private long sensorDetached;
	private long noSensor;

	private ToolingStatusSummary lowStatusSummary;
	private ToolingStatusSummary mediumStatusSummary;
	private ToolingStatusSummary highStatusSummary;
	private ToolingStatusSummary prolongedStatusSummary;

	private UtilizationConfig config;

	public long getTotal() {
		return low + medium + high + prolonged;
	}

	public BigDecimal getLowRate() {
		return ValueUtils.toRate(low, getTotal());
	}

	public BigDecimal getMediumRate() {
		return ValueUtils.toRate(medium, getTotal());
	}

	public BigDecimal getHighRate() {
		return ValueUtils.toRate(high, getTotal());
	}

	public BigDecimal getProlongedRate() {
		return ValueUtils.toRate(prolonged, getTotal());
	}
}
