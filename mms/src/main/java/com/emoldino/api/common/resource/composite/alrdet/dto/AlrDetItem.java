package com.emoldino.api.common.resource.composite.alrdet.dto;

import java.time.Instant;

import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.util.DateUtils2;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.CounterStatus;

@Data
@NoArgsConstructor
public class AlrDetItem {
	private Long id;
	private String detachmentDateTime;
	private String repairDateTime;
	private String confirmDateTime;

	private Long moldId;
	private String moldCode;
	private ToolingStatus toolingStatus;
	private CounterStatus sensorStatus;

	private Long companyId;
	private String companyName;
	private String companyCode;
	private CompanyType companyType;

	private Long locationId;
	private String locationName;
	private String locationCode;

	private Long machineId;
	private String machineCode;

	public AlrDetItem(Long id, Instant detachmentTime, Instant repairTime, Instant confirmedAt, //
					  Long moldId, String moldCode, ToolingStatus toolingStatus, String sensorStatus, //
					  Long companyId, String companyName, String companyCode, CompanyType companyType, //
					  Long locationId, String locationName, String locationCode, //
					  Long machineId, String machineCode//
	) {
		String zoneId = LocationUtils.getZoneIdByLocationId(locationId);
		this.id = id;
		this.detachmentDateTime = DateUtils2.format(detachmentTime, DateUtils2.DatePattern.yyyy_MM_dd_HH_mm_ss, zoneId);
		this.repairDateTime = DateUtils2.format(repairTime, DateUtils2.DatePattern.yyyy_MM_dd_HH_mm_ss, zoneId);
		this.confirmDateTime = DateUtils2.format(confirmedAt, DateUtils2.DatePattern.yyyy_MM_dd_HH_mm_ss, zoneId);

		this.moldId = moldId;
		this.moldCode = moldCode;
		this.toolingStatus = toolingStatus;
		this.sensorStatus = sensorStatus == null ? null : CounterStatus.valueOf(sensorStatus);

		this.companyId = companyId;
		this.companyName = companyName;
		this.companyCode = companyCode;
		this.companyType = companyType;

		this.locationId = locationId;
		this.locationName = locationName;
		this.locationCode = locationCode;

		this.machineId = machineId;
		this.machineCode = machineCode;
	}
}
