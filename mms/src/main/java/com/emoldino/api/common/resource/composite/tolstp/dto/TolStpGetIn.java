package com.emoldino.api.common.resource.composite.tolstp.dto;

import java.util.List;

import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;

import io.swagger.annotations.ApiParam;
import lombok.Data;
import saleson.common.enumeration.CounterStatus;

@Data
public class TolStpGetIn {
	private String query;
	private String filterCode;
	private String tabName;

	@ApiParam(hidden = true, defaultValue = "true")
	private Boolean enabled;

	private List<ToolingStatus> toolingStatus;
	private List<CounterStatus> counterStatus;
//	private String accumulatedShotFilter;
	private List<Long> id;
	//for direct link
	private Long companyId;
	private Long partId;

}
