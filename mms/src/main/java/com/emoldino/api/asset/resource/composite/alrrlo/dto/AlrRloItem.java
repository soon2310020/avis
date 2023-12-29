package com.emoldino.api.asset.resource.composite.alrrlo.dto;

import java.time.Instant;

import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.util.DateUtils2;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.CounterStatus;
import saleson.common.enumeration.MoldLocationStatus;

@Data
@NoArgsConstructor
public class AlrRloItem {
	private Long id;
	private MoldLocationStatus moldLocationStatus;
	private String creationDateTime;
	private String confirmDateTime;

	private Long moldId;
	private String moldCode;
	private ToolingStatus toolingStatus;
	private CounterStatus sensorStatus;

	private Long companyId;
	private String companyName;
	private String companyCode;
	private CompanyType companyType;

	private Long previousLocationId;
	private String previousLocationName;
	private String previousLocationCode;

	private Long locationId;
	private String locationName;
	private String locationCode;

	public AlrRloItem(//
			Long id, MoldLocationStatus moldLocationStatus, Instant createdAt, Instant confirmedAt, //
			Long moldId, String moldCode, ToolingStatus toolingStatus, String sensorStatus, //
			Long companyId, String companyName, String companyCode, CompanyType companyType, //
			Long previousLocationId, String previousLocationName, String previousLocationCode, //
			Long locationId, String locationName, String locationCode//
	) {
		String zoneId = LocationUtils.getZoneIdByLocationId(locationId);
		this.id = id;
		this.moldLocationStatus = moldLocationStatus;
		this.creationDateTime = DateUtils2.format(createdAt, DateUtils2.DatePattern.yyyy_MM_dd_HH_mm_ss, zoneId);
		this.confirmDateTime = DateUtils2.format(confirmedAt, DateUtils2.DatePattern.yyyy_MM_dd_HH_mm_ss, zoneId);

		this.moldId = moldId;
		this.moldCode = moldCode;
		this.toolingStatus = toolingStatus;
		this.sensorStatus = sensorStatus == null ? null : CounterStatus.valueOf(sensorStatus);

		this.companyId = companyId;
		this.companyName = companyName;
		this.companyCode = companyCode;
		this.companyType = companyType;

		this.previousLocationId = previousLocationId;
		this.previousLocationCode = previousLocationCode;
		this.previousLocationName = previousLocationName;

		this.locationId = locationId;
		this.locationName = locationName;
		this.locationCode = locationCode;
	}
}
