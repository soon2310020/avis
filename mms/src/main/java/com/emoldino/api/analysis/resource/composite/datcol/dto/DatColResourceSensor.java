package com.emoldino.api.analysis.resource.composite.datcol.dto;

import java.time.Instant;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import lombok.Data;
import saleson.common.enumeration.BatteryStatus;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.OperatingStatus;
import saleson.common.enumeration.PresetStatus;

@Data
public class DatColResourceSensor {
	private String equipmentCode;

	@Enumerated(EnumType.STRING)
	private EquipmentStatus equipmentStatus;
	@Enumerated(EnumType.STRING)
	private OperatingStatus operatingStatus;

	private String companyCode;
	private String locationCode;

	private String purchasedAt;
	private String installedBy;
	private String installedAt;
	private Instant activatedAt;
	private Instant operatedAt;

	private String memo;
	private Integer shotCount;
	private Instant lastShotAt; // 마지막 Shot 날짜
	private Instant lastShotAtVal;

	private Integer presetCount; // Preset 설정 카운터
	@Enumerated(EnumType.STRING)
	private PresetStatus presetStatus;
	@Enumerated(EnumType.STRING)
	private BatteryStatus batteryStatus;

	private boolean enabled;
}
