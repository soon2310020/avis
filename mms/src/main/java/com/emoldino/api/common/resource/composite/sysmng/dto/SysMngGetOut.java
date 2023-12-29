package com.emoldino.api.common.resource.composite.sysmng.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.emoldino.framework.util.ValueUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class SysMngGetOut {
	private String clientName;
	private String serverName;
	private String licenseType;
	private Boolean emoldino;
	private Boolean serverNameMatched;
	private long terminalConCount;
	private long terminalDisconCount;
	private long terminalHoldCount;
	private long terminalAbnormalCount;
	private long terminalCount;
	private long sensorConCount;
	private long sensorDisconCount;
	private long sensorHoldCount;
	private long sensorAbnormalCount;
	private long sensorCount;
	private long userAbnormalCount;
	private long plantAbnormalCount;
	private String serverTime;
	private String serverTimeSys;
	private String dbTime;
	private String dbTimeSys;
	private String timeZoneId;
	private String offset;
	private int minsDiffWithDbTime;
	private int minsDiffWithFirstServer;
	private String clientUrl;
	private String errorMessage;

	private List<SysMngDevice> abnormalTerminals;
	private List<SysMngDevice> abnormalSensors;
	private List<SysMngDevice> abnormalToolings;
	private List<SysMngDevice> abnormalCodes;

	public BigDecimal getTerminalConRate() {
		return r(terminalCount - terminalHoldCount == 0 ? 0d : ((double) terminalConCount / (double) (terminalCount - terminalHoldCount)));
	}

	public BigDecimal getSensorConRate() {
		return r(sensorCount - sensorHoldCount == 0 ? 0d : ((double) sensorConCount / (double) (sensorCount - sensorHoldCount)));
	}

	public void addAbnormalTerminal(SysMngDevice terminal) {
		if (this.abnormalTerminals == null) {
			this.abnormalTerminals = new ArrayList<>();
		}
		this.abnormalTerminals.add(terminal);
	}

	public void addAbnormalSensor(SysMngDevice sensor) {
		if (this.abnormalSensors == null) {
			this.abnormalSensors = new ArrayList<>();
		}
		this.abnormalSensors.add(sensor);
	}

	private BigDecimal r(double value) {
		return ValueUtils.toBigDecimal(value, 3, 0d);
	}
}
