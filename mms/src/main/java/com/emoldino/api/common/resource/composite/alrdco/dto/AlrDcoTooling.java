package com.emoldino.api.common.resource.composite.alrdco.dto;

import java.time.Instant;

import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.util.DateUtils2;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.CounterStatus;
import saleson.common.enumeration.NotificationStatus;

@Data
@NoArgsConstructor
public class AlrDcoTooling {
	private Long id;
	private NotificationStatus alertStatus;
	private String creationDateTime;
	private String confirmUserName;
	private String confirmDateTime;

	private Long moldId;
	private String moldCode;
	private ToolingStatus toolingStatus;
	private CounterStatus sensorStatus;
	private String lastShotDateTime;

	private Long companyId;
	private String companyName;
	private String companyCode;
	private CompanyType companyType;

	private Long locationId;
	private String locationCode;
	private String locationName;

	private String areaName;

	public AlrDcoTooling(//
						 Long id, NotificationStatus alertStatus, Instant createdAt, String confirmUserName, Instant confirmedAt, //
						 Long moldId, String moldCode, ToolingStatus toolingStatus, String sensorStatus, Instant lastShotAt, //
						 Long companyId, String companyName, String companyCode, CompanyType companyType, //
						 Long locationId, String locationName, String locationCode, //
						 String areaName//
	) {
		// TODO UserTimeZone?
		String zoneId = LocationUtils.getZoneIdByLocationId(locationId);

		this.id = id;
		this.alertStatus = alertStatus;
		this.creationDateTime = DateUtils2.format(createdAt, DateUtils2.DatePattern.yyyy_MM_dd_HH_mm_ss, zoneId);
		this.confirmUserName = confirmUserName;
		this.confirmDateTime = DateUtils2.format(confirmedAt, DateUtils2.DatePattern.yyyy_MM_dd_HH_mm_ss, zoneId);

		this.moldId = moldId;
		this.moldCode = moldCode;
		this.toolingStatus = toolingStatus;
		this.sensorStatus = sensorStatus == null ? null : CounterStatus.valueOf(sensorStatus);
		this.lastShotDateTime = DateUtils2.format(lastShotAt, DateUtils2.DatePattern.yyyy_MM_dd_HH_mm_ss, zoneId);

		this.companyId = companyId;
		this.companyName = companyName;
		this.companyCode = companyCode;
		this.companyType = companyType;

		this.locationId = locationId;
		this.locationName = locationName;
		this.locationCode = locationCode;
		this.areaName = areaName;
	}
}
