package com.emoldino.api.common.resource.composite.ssrstp.dto;

import java.time.Instant;

import com.emoldino.api.common.resource.base.location.enumeration.AreaType;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.ValueUtils;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.BatteryStatus;
import saleson.common.enumeration.CounterStatus;
import saleson.common.enumeration.PresetStatus;
import saleson.model.Counter;
import saleson.model.Mold;

@Data
@NoArgsConstructor
public class SsrStpItem {
	private Long id;

	// Sensor ID
	private String equipmentCode;

	// Sensor Status
	private CounterStatus sensorStatus;
	// Battery
	private BatteryStatus batteryStatus;
	@Enumerated(EnumType.STRING)
	private PresetStatus presetStatus;
	private boolean enabled;

	// Company
	private Long companyId;
	private String companyName;
	private String companyCode;

	// Plant
	private Long locationId;
	private String locationName;
	private String locationCode;

	// Tooling ID
	private Long moldId;
	private String moldCode;
	// Tooling Status
	private ToolingStatus toolingStatus;

	// Installation Personnel
	private String installedBy;
	// Installation Date
	private String installedAt;
	// Activation Date
	private String activationDate;
	private String lastShotDateTime;

	// Shot Number
	private Integer shotCount;
	// Sensor Reset action -> Reset Value
	private Integer presetCount;

	// Memo
	private String memo;

	// Subscription Term
	private Integer subscriptionTerm;
	// Subscription Status
	private String subscriptionStatus;
	// Subscription Expiry
	private String subscriptionExpiry;
	private String activePeriod;

	private String lastWorkOrderDate;
	private Long workOrderCount;

	//
	private String supplierMoldCode;

	private String areaName;
	private AreaType areaType;

	public SsrStpItem(Counter counter, Instant lastWorkOrderAt, Long workOrderCount) {
		ValueUtils.map(counter, this);

		this.sensorStatus = counter.getCounterStatus();

		Mold mold = counter.getMold();
		if (mold == null) {
			this.toolingStatus = ToolingStatus.NO_SENSOR;
		} else {
			this.moldId = mold.getId();
			this.moldCode = mold.getEquipmentCode();
			this.toolingStatus = mold.getToolingStatus();
			this.supplierMoldCode = mold.getSupplierMoldCode();
			this.areaName = mold.getAreaName();
			this.areaType = mold.getAreaType();
		}

		String zoneId = LocationUtils.getZoneIdByLocationId(counter.getLocationId());

		this.activationDate = DateUtils2.format(counter.getActivatedAt(), DatePattern.yyyy_MM_dd, zoneId);
		this.lastShotDateTime = DateUtils2.format(counter.getLastShotAt(), DatePattern.yyyy_MM_dd_HH_mm_ss, zoneId);

		this.lastWorkOrderDate = DateUtils2.format(lastWorkOrderAt, DatePattern.yyyy_MM_dd, zoneId);
		this.workOrderCount = workOrderCount;


	}
}
