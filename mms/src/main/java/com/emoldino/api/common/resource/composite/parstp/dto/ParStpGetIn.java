package com.emoldino.api.common.resource.composite.parstp.dto;

import java.util.List;

import com.emoldino.framework.enumeration.TimeScale;

import io.swagger.annotations.ApiParam;
import lombok.Data;

@Data
public class ParStpGetIn {
	private String query;
	private String filterCode;

	private String tabName;
	@ApiParam(hidden = true, defaultValue = "true")
	private Boolean enabled;
	private TimeScale timeScale;
	private String timeValue;
	private List<Long> id;
}
