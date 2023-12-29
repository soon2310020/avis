package com.emoldino.api.analysis.resource.composite.tolrpt.dto;

import io.swagger.annotations.ApiParam;
import lombok.Data;

@Data
public class TolRptGetIn {
	private String query;

	@ApiParam(hidden = true, defaultValue = "true")
	private Boolean enabled;

	public String getFilterCode() {
		return "COMMON";
	}
}
