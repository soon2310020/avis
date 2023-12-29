package com.emoldino.api.common.resource.composite.ssrstp.dto;

import java.util.List;

import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;

import io.swagger.annotations.ApiParam;
import lombok.Data;
import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.CounterStatus;

@Data
public class SsrStpGetIn {
	private String query;
	private String filterCode;

	private String tabName;
	@ApiParam(hidden = true, defaultValue = "true")
	private Boolean enabled;
	@ApiParam(hidden = true)
	private CompanyType companyType;

	private List<ToolingStatus> toolingStatus;
	private List<CounterStatus> sensorStatus;

	@Deprecated
	public void setCounterStatus(List<CounterStatus> counterStatus) {
		this.sensorStatus = counterStatus;
	}

}
