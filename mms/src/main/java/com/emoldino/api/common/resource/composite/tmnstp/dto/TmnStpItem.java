package com.emoldino.api.common.resource.composite.tmnstp.dto;

import java.time.Instant;

import com.emoldino.api.common.resource.base.location.enumeration.AreaType;
import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.api.common.resource.composite.tmnstp.enumeration.TmnStpConnectionStatus;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.IpType;

@Data
@NoArgsConstructor
public class TmnStpItem {
	private Long id;

	// Terminal ID
	private String equipmentCode;
	// Terminal Status
	private EquipmentStatus equipmentStatus;

	// Connection Status
	private TmnStpConnectionStatus connectionStatus;
	// Last Connection
	private String operationDateTime;
	// No. of Sensor
	private long counterCount;

	// Company
	private Long companyId;
	private String companyName;
	private String companyCode;

	// Plant
	private Long locationId;
	private String locationName;
	private String locationCode;

	// Plant Area
	private Long areaId;
	private String areaName;

	// Installation Personnel
	private String installedBy;
	// Installation Date
	private String installedAt;
	// Activation Date
	private String activationDate;

	// Memo
	private String memo;

	private IpType ipType;
	private String ipAddress;
	private String subnetMask;
	private String gateway;
	private String dns;

	private String lastWorkOrderDate;
	private Long workOrderCount;

	private Boolean locationEnabled;
	private String createdAtDate;
	public AreaType areaType;

	public TmnStpItem(//
			Long id, String equipmentCode, EquipmentStatus equipmentStatus, //
			String connectionStatus, Instant operatedAt, Long counterCount, //
			Long companyId, String companyName, String companyCode, //
			Long locationId, String locationName, String locationCode, //
			Boolean locationEnabled, //
			Long areaId, String areaName, AreaType areaType, //
			String installationArea, String installedBy, String installedAt, Instant activatedAt, //
			String memo, //
			IpType ipType, String ipAddress, String subnetMask, String gateway, String dns, //
			Instant createdAt, Instant lastWorkOrderAt, Long workOrderCount//
	) {
		String zoneId = LocationUtils.getZoneIdByLocationId(locationId);

		this.id = id;
		this.equipmentCode = equipmentCode;
		this.equipmentStatus = equipmentStatus;

		this.connectionStatus = connectionStatus == null ? null : TmnStpConnectionStatus.valueOf(connectionStatus);
		this.operationDateTime = DateUtils2.format(operatedAt, DatePattern.yyyy_MM_dd_HH_mm_ss, zoneId);
		this.counterCount = counterCount;

		this.companyId = companyId;
		this.companyName = companyName;
		this.companyCode = companyCode;

		this.locationId = locationId;
		this.locationName = locationName;
		this.locationCode = locationCode;
		this.locationEnabled = locationEnabled != null ? locationEnabled : false;

		this.areaId = areaId;
		this.areaName = areaName;
		this.areaType = areaType;

		// this.installedArea = installedArea;
		this.installedBy = installedBy;
		this.installedAt = installedAt;
		this.activationDate = DateUtils2.format(activatedAt, DatePattern.yyyy_MM_dd, zoneId);
		this.memo = memo;

		this.ipType = ipType;
		this.ipAddress = ipAddress;
		this.subnetMask = subnetMask;
		this.gateway = gateway;
		this.dns = dns;
		this.createdAtDate = DateUtils2.format(createdAt, DatePattern.yyyy_MM_dd, zoneId);

		this.lastWorkOrderDate = DateUtils2.format(lastWorkOrderAt, DatePattern.yyyy_MM_dd, zoneId);
		this.workOrderCount = workOrderCount;
	}
}
