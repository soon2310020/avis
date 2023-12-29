package com.emoldino.api.production.resource.composite.alrutm.dto;

import java.time.Instant;

import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.util.DateUtils2;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.CounterStatus;
import saleson.common.enumeration.EfficiencyStatus;
import saleson.common.enumeration.NotificationStatus;
import saleson.common.enumeration.OperatingStatus;

@Data
@NoArgsConstructor
public class AlrUtmItem {
	private Long id;

	private NotificationStatus alertStatus; // Status
	private String creationDateTime; // CREATED_AT
	private String confirmedDateTime; // Confirmation Date

	private EfficiencyStatus uptimeStatus; // EFFICIENCY_STATUS
	private Double baseEfficiency; // BASE_EFFICIENCY
	private Double efficiency; // EFFICIENCY
	private Double variance; // (efficiency - baseEfficiency * 100) / baseEfficiency;

	private Long moldId;
	private String moldCode;
	private ToolingStatus toolingStatus;
	private OperatingStatus operatingStatus; // OP
	private CounterStatus sensorStatus;

	private Long companyId;
	private String companyName;
	private String companyCode;
	private CompanyType companyType;

	private Long locationId;
	private String locationCode;
	private String locationName;

	private Long machineId;
	private String machineCode;

	public AlrUtmItem(//
			Long id, //
			NotificationStatus alertStatus, Instant creationDateTime, Instant confirmedDateTime, //
			EfficiencyStatus uptimeStatus, Double baseEfficiency, Double efficiency, Double variance, //
			Long moldId, String moldCode, ToolingStatus toolingStatus, OperatingStatus operatingStatus, String sensorStatus, //
			Long companyId, String companyName, String companyCode, CompanyType companyType, //
			Long locationId, String locationCode, String locationName, //
			Long machineId, String machineCode//
	) {

		String zoneId = LocationUtils.getZoneIdByLocationId(locationId);

		this.id = id;

		this.alertStatus = alertStatus;
		this.creationDateTime = DateUtils2.format(creationDateTime, DateUtils2.DatePattern.yyyy_MM_dd_HH_mm_ss, zoneId);
		this.confirmedDateTime = DateUtils2.format(confirmedDateTime, DateUtils2.DatePattern.yyyy_MM_dd_HH_mm_ss, zoneId);
		this.uptimeStatus = uptimeStatus;
		this.baseEfficiency = baseEfficiency;
		this.efficiency = efficiency;
		this.variance = variance;

		this.moldId = moldId;
		this.moldCode = moldCode;
		this.toolingStatus = toolingStatus;
		this.operatingStatus = operatingStatus;
		this.sensorStatus = sensorStatus == null ? null : CounterStatus.valueOf(sensorStatus);

		this.companyId = companyId;
		this.companyName = companyName;
		this.companyCode = companyCode;
		this.companyType = companyType;

		this.locationId = locationId;
		this.locationCode = locationCode;
		this.locationName = locationName;

		this.machineId = machineId;
		this.machineCode = machineCode;
	}
}
