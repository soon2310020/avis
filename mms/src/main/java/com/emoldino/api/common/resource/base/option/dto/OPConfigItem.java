package com.emoldino.api.common.resource.base.option.dto;

import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.EquipmentType;
import saleson.common.enumeration.OperatingStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class OPConfigItem {
	private EquipmentType equipmentType;
	private OperatingStatus operatingStatus;
	private Double time;
	private ChronoUnit timeUnit;
}
