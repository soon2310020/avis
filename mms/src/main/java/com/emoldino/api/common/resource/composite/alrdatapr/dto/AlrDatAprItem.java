package com.emoldino.api.common.resource.composite.alrdatapr.dto;

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
public class AlrDatAprItem {
	private Long id;
	private NotificationStatus notificationStatus;
	private String creationDateTime;
	private String approvalDateTime;
	private String approvalBy;
	private String reason;

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

	public AlrDatAprItem(//
			Long id, NotificationStatus notificationStatus, Instant createdAt, Instant approvedAt, //
			String approvalBy, String reason,
			Long moldId, String moldCode, ToolingStatus toolingStatus, String sensorStatus, //
			Long companyId, String companyName, String companyCode, CompanyType companyType, //
			Long locationId, String locationName, String locationCode//
	) {
		String zoneId = LocationUtils.getZoneIdByLocationId(locationId);
		this.id = id;
		this.notificationStatus = notificationStatus;
		this.creationDateTime = DateUtils2.format(createdAt, DateUtils2.DatePattern.yyyy_MM_dd_HH_mm_ss, zoneId);
		this.approvalDateTime = DateUtils2.format(approvedAt, DateUtils2.DatePattern.yyyy_MM_dd_HH_mm_ss, zoneId);
		this.approvalBy =approvalBy;
		this.reason = reason;

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
	}
}
