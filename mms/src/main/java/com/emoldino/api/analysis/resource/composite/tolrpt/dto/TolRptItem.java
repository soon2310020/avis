package com.emoldino.api.analysis.resource.composite.tolrpt.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.api.common.resource.base.object.dto.FieldValue;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.ValueUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TolRptItem {
	private Long moldId;
	private String moldCode;
	private ToolingStatus toolingStatus;

	private Long locationId;
	private String locationName;
	private String locationCode;

	private String areaName;

	private BigDecimal utilizationRate;
	private Integer accumShotCount;
	private Integer warrantyShotCount;
	private String lastShotDate;
	private String lastShotDateTime;

	private BigDecimal lastCycleTime;
	private BigDecimal weightedAverageCycleTime;

	private Long supplierId;
	private String supplierName;
	private String supplierCode;
	private Integer prodDays;

	private Integer totalCavities;
	private BigDecimal machineTonnage;
	private BigDecimal quotedMachineTonnage;
	private BigDecimal weightRunner;

	private List<FieldValue> customFields;
	private List<TolRptPart> parts;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class TolRptPart {
		private Long id;
		private String name;
		private String partCode;
		private String weight;
	}

	public TolRptItem(//
			Long moldId, String moldCode, ToolingStatus toolingStatus, //
			Long locationId, String locationName, String locationCode, String areaName, //
			Double utilizationRate, Integer accumShotCount, Integer warrantyShotCount, Instant lastShotAt, Double lastCycleTime, Double weightedAverageCycleTime,
			Long supplierId, String supplierName, String supplierCode, String prodDays, //
			Integer totalCavities, Double machineTonnage, Double quotedMachineTonnage, Double weightRunner//
	) {
		String zoneId = LocationUtils.getZoneIdByLocationId(locationId);

		this.moldId = moldId;
		this.moldCode = moldCode;
		this.toolingStatus = toolingStatus;

		this.locationId = locationId;
		this.locationName = locationName;
		this.locationCode = locationCode;
		this.areaName = areaName;

		this.utilizationRate = ValueUtils.toBigDecimal(utilizationRate, 0d);
		this.accumShotCount = accumShotCount;
		this.warrantyShotCount = warrantyShotCount;
		this.lastShotDate = DateUtils2.format(lastShotAt, DatePattern.yyyy_MM_dd, zoneId);
		this.lastShotDateTime = DateUtils2.format(lastShotAt, DatePattern.yyyy_MM_dd_HH_mm, zoneId);
		this.lastCycleTime = ValueUtils.toBigDecimal(lastCycleTime, 0d);
		this.weightedAverageCycleTime = ValueUtils.toBigDecimal(weightedAverageCycleTime, 0d);

		this.supplierId = supplierId;
		this.supplierName = supplierName;
		this.supplierCode = supplierCode;
		this.prodDays = ValueUtils.toInteger(prodDays, 0);

		this.totalCavities = totalCavities;
		this.machineTonnage = ValueUtils.toBigDecimal(machineTonnage, 0d);
		this.quotedMachineTonnage = ValueUtils.toBigDecimal(quotedMachineTonnage, 0d);
		this.weightRunner = ValueUtils.toBigDecimal(weightRunner, 0d);
	}

}
