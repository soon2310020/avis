package com.emoldino.api.common.resource.composite.sysmng.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.OperatingStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class SysMngDevice {
	private String clientName;
	private String code;
	private EquipmentStatus status;
	private OperatingStatus opStatus;
	private Boolean enabled;
	private String code2;
	private EquipmentStatus status2;
	private OperatingStatus opStatus2;
	private Boolean enabled2;

	public SysMngDevice(String code, EquipmentStatus status) {
		this.code = code;
		this.status = status;
	}

	public SysMngDevice(String code, Boolean enabled, EquipmentStatus status, OperatingStatus opStatus) {
		this.code = code;
		this.enabled = enabled;
		this.status = status;
		this.opStatus = opStatus;
	}

	public SysMngDevice(//
			String code, Boolean enabled, EquipmentStatus status, OperatingStatus opStatus, //
			String code2, Boolean enabled2, EquipmentStatus status2, OperatingStatus opStatus2//
	) {
		this.code = code;
		this.enabled = enabled;
		this.status = status;
		this.opStatus = opStatus;
		this.code2 = code2;
		this.enabled2 = enabled2;
		this.status2 = status2;
		this.opStatus2 = opStatus2;
	}
}
