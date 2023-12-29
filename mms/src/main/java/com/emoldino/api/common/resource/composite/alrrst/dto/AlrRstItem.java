package com.emoldino.api.common.resource.composite.alrrst.dto;

import java.time.Instant;

import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.util.DateUtils2;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.CounterStatus;
import saleson.common.enumeration.MisconfigureStatus;

@Data
@NoArgsConstructor
public class AlrRstItem {
	private Long id;
	private MisconfigureStatus alertStatus;
	private String creationUserName;
	private String creationDateTime;//Alert Date
	private String confirmUserName;
	private String confirmDateTime;

	private Long moldId;
	private String moldCode;
	private ToolingStatus toolingStatus;

	private Long counterId;
	private String counterCode;
	private CounterStatus sensorStatus;

	private Integer accumShotCount;
//	private String lastShotDateTime;
	private Integer resetValue;

	private Long companyId;
	private String companyName;
	private String companyCode;
	private CompanyType companyType;

	private Long locationId;
	private String locationCode;
	private String locationName;

	public AlrRstItem(//
					  Long id, //
					  MisconfigureStatus alertStatus, String creationUserName, Instant createdAt, String confirmUserName, Instant confirmedAt, //
					  Long moldId, String moldCode, ToolingStatus toolingStatus, //
					  Long counterId, String counterCode, String sensorStatus, //
					  Integer accumShotCount, // Instant lastShotAt, //
					  Integer resetValue, //
					  Long companyId, String companyName, String companyCode, CompanyType companyType, //
					  Long locationId, String locationCode, String locationName //
					  //
	) {
		// TODO UserTimeZone?
		String zoneId = LocationUtils.getZoneIdByLocationId(locationId);

		this.id = id;
		this.alertStatus = alertStatus;
		this.creationUserName = creationUserName;
		this.creationDateTime = DateUtils2.format(createdAt, DateUtils2.DatePattern.yyyy_MM_dd_HH_mm_ss, zoneId);
		this.confirmUserName = confirmUserName;
		this.confirmDateTime = DateUtils2.format(confirmedAt, DateUtils2.DatePattern.yyyy_MM_dd_HH_mm_ss, zoneId);

		this.moldId = moldId;
		this.moldCode = moldCode;
		this.toolingStatus = toolingStatus;
//		this.lastShotDateTime = DateUtils2.format(lastShotAt, DateUtils2.DatePattern.yyyy_MM_dd_HH_mm_ss, zoneId);
		this.accumShotCount = accumShotCount;
		this.resetValue = resetValue;

		this.counterId = counterId;
		this.counterCode = counterCode;
		this.sensorStatus = sensorStatus == null ? null : CounterStatus.valueOf(sensorStatus);

		this.companyId = companyId;
		this.companyName = companyName;
		this.companyCode = companyCode;
		this.companyType = companyType;

		this.locationId = locationId;
		this.locationName = locationName;
		this.locationCode = locationCode;
	}
}
