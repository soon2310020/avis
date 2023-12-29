package com.emoldino.api.common.resource.composite.flt.dto;

import com.emoldino.framework.util.ValueUtils;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Machine;

@Data
@NoArgsConstructor
public class FltMachine {
	private Long id;
	private String machineCode;

	public FltMachine(Machine machine) {
		ValueUtils.map(machine, this);
	}

	public String getName() {
		return machineCode;
	}

	public String getCode() {
		return machineCode;
	}
}
