package com.emoldino.api.common.resource.composite.mchstp.dto;

import java.util.List;

import io.swagger.annotations.ApiParam;
import lombok.Data;

@Data
public class MchStpGetIn {
	private String query;
	private String filterCode;

	private String tabName;
	@ApiParam(hidden = true, defaultValue = "true")
	private Boolean enabled;
	@ApiParam(hidden = true)
	private Boolean matchedWithTooling;
	private List<Long> id;
}
