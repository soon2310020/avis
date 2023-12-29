package com.emoldino.api.common.resource.composite.mchstp.dto;

import java.time.Instant;
import java.util.List;

import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.api.common.resource.base.object.dto.FieldValue;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.ValueUtils;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Machine;

@Data
@NoArgsConstructor
public class MchStpItem {
	private Long id;

	// Machine ID
	private String machineCode;

	//Tooling ID
	private Long moldId;
	private String moldCode;
	private String moldUpdatedAtDate;

	// Company
	private Long companyId;
	private String companyName;
	private String companyCode;

	// Plant
	private Long locationId;
	private String locationName;
	private String locationCode;

	// Line
	private String line;

	// Machine Type
	private String machineType;
	// Machine Maker
	private String machineMaker;
	// Machine Model
	private String machineModel;
	// Machine Tonnage
	private Integer machineTonnage;

	private String creationDate;

	private String lastWorkOrderDate;
	private Long workOrderCount;

	private List<FieldValue> customFields;

	public MchStpItem(Machine machine, Instant lastWorkOrderAt, Long workOrderCount) {
		ValueUtils.map(machine, this);

		String zoneId = LocationUtils.getZoneIdByLocationId(locationId);
		if (machine.getMold() != null && !machine.getMold().isDeleted()) {
			this.moldId = machine.getMold().getId();
			this.moldCode = machine.getMold().getEquipmentCode();
			this.moldUpdatedAtDate = DateUtils2.format(machine.getUpdatedAt(), DatePattern.yyyy_MM_dd, zoneId);
		}

		// TODO by user timezone
		this.creationDate = DateUtils2.format(machine.getCreatedAt(), DatePattern.yyyy_MM_dd, zoneId);

		this.lastWorkOrderDate = DateUtils2.format(lastWorkOrderAt, DatePattern.yyyy_MM_dd, zoneId);
		this.workOrderCount = workOrderCount;
	}
}
