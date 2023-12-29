package com.emoldino.api.integration.resource.composite.inftol.dto;

import java.math.BigDecimal;

import com.emoldino.framework.util.ValueUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InfTolSummaryItem {
	@JsonInclude(Include.NON_NULL)
	private String sensorId;
	@JsonInclude(Include.NON_NULL)
	private String toolingId;
	private long shotCount;
	private BigDecimal cycleTime;
	private long uptimeSeconds;

	public InfTolSummaryItem() {
		this(null, null, 0L, ValueUtils.toBigDecimal(0d, 0d), 0L);
	}

	public InfTolSummaryItem(String sensorId, String toolingId, Integer shotCount, Double cycleTime, Long uptimeSeconds) {
		this(sensorId, toolingId, ValueUtils.toLong(shotCount, 0L), ValueUtils.toBigDecimal(cycleTime, 0d), ValueUtils.toLong(uptimeSeconds, 0L));
	}

	public InfTolSummaryItem(String toolingId, Integer shotCount, Double cycleTime, Long uptimeSeconds) {
		this(null, toolingId, ValueUtils.toLong(shotCount, 0L), ValueUtils.toBigDecimal(cycleTime, 0d), ValueUtils.toLong(uptimeSeconds, 0L));
	}
}
