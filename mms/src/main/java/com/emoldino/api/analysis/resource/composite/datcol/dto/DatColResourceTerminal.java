package com.emoldino.api.analysis.resource.composite.datcol.dto;

import java.time.Instant;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import lombok.Data;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.IpType;
import saleson.common.enumeration.OperatingStatus;

@Data
public class DatColResourceTerminal {
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

	private String installationArea;
	private IpType ipType;

	private String ipAddress;
	private String subnetMask;
	private String gateway;
	private String dns;

	private boolean enabled;
}
