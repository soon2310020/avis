package com.emoldino.api.common.resource.composite.flt.dto;

import com.emoldino.framework.util.ValueUtils;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Mold;

@Data
@NoArgsConstructor
public class FltMold {
	private Long id;
	private String equipmentCode;

	public FltMold(Mold mold) {
		ValueUtils.map(mold, this);
	}

	public String getName() {
		return equipmentCode;
	}

	public String getCode() {
		return equipmentCode;
	}
}
