package com.emoldino.api.common.resource.composite.flt.dto;

import com.emoldino.framework.util.ValueUtils;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Location;

@Data
@NoArgsConstructor
public class FltPlant {
	private Long id;
	private String name;
	private String locationCode;

	public FltPlant(Location location) {
		ValueUtils.map(location, this);
	}

	public String getCode() {
		return locationCode;
	}
}
