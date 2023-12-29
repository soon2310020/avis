package com.emoldino.api.asset.resource.composite.toleol.dto;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingUtilizationStatus;
import com.emoldino.api.asset.resource.base.mold.util.MoldUtils;
import com.emoldino.api.asset.resource.composite.toleol.enumeration.TolEolType;
import com.emoldino.api.common.resource.base.option.dto.UtilizationConfig;
import com.emoldino.api.common.resource.composite.flt.dto.FltPart;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.ValueUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.QueryProjection;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.PriorityType;

@Data
@NoArgsConstructor
public class TolEolItem {
	private Long id;
	private String moldCode;
	private ToolingStatus toolingStatus;
	@JsonIgnore
	private String firstPartName;
	private List<FltPart> parts;
	private Integer accumShotCount;
	private Integer designedShotCount;
	private Double utilizationRate;
	private ToolingUtilizationStatus utilizationStatus;
	private Integer remainingDays;
	private String zoneId;
	private PriorityType refurbPriority;
	private TolEolType eolType;
	private String eolDate;
	private BigDecimal cost;
	private BigDecimal salvageValue;
	private String currencyCode;
	private String currencySymbol;

	@QueryProjection
	public TolEolItem(//
			Long id, String moldCode, ToolingStatus toolingStatus, //
			String firstPartName, //
			int accumShotCount, int designedShotCount, Double utilizationRate, //
			Integer remainingDays, String zoneId, String refurbPriority, //
			Double cost, Double salvageValue//
	) {
		this.id = id;
		this.moldCode = moldCode;
		this.toolingStatus = toolingStatus;
		this.firstPartName = firstPartName;
		this.designedShotCount = designedShotCount;
		this.accumShotCount = accumShotCount;
		setUtilizationRate(utilizationRate);
		setRemainingDays(remainingDays);
		this.zoneId = zoneId;
		this.refurbPriority = refurbPriority == null ? null : PriorityType.valueOf(refurbPriority);
		this.cost = ValueUtils.toBigDecimal(cost, null);
		this.salvageValue = ValueUtils.toBigDecimal(salvageValue, null);
	}

	public void setUtilizationRate(Double utilizationRate) {
		this.utilizationStatus = null;
		this.utilizationRate = utilizationRate;
		if (utilizationRate == null) {
			return;
		}
		UtilizationConfig config = MoldUtils.getUtilizationConfig();
		if (utilizationRate <= config.getLow()) {
			this.utilizationStatus = ToolingUtilizationStatus.LOW;
		} else if (utilizationRate <= config.getMedium()) {
			this.utilizationStatus = ToolingUtilizationStatus.MEDIUM;
		} else if (utilizationRate <= config.getHigh()) {
			this.utilizationStatus = ToolingUtilizationStatus.HIGH;
		} else {
			this.utilizationStatus = ToolingUtilizationStatus.PROLONGED;
		}
	}

	public void setRemainingDays(Integer remainingDays) {
		this.eolType = null;
		this.eolDate = null;
		this.remainingDays = remainingDays;
		if (remainingDays == null) {
			return;
		}
		Instant eolAt = DateUtils2.getInstant().plus(Duration.ofDays(remainingDays));
		if (remainingDays <= 0) {
			eolType = TolEolType.COMPLETED;
		} else if (remainingDays > 3650) {
			eolType = TolEolType.UNPREDICTABLE;
		} else {
			eolType = TolEolType.REACHING;
		}
		this.eolDate = DateUtils2.format(eolAt, DatePattern.yyyy_MM_dd, ValueUtils.toString(zoneId, Zone.SYS));
	}
}
