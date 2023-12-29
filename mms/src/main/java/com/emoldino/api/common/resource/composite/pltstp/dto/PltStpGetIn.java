package com.emoldino.api.common.resource.composite.pltstp.dto;

import java.util.List;

import io.swagger.annotations.ApiParam;
import lombok.Data;
import saleson.common.enumeration.CompanyType;

@Data
public class PltStpGetIn {
	private String query;
	private String filterCode;

	private String tabName;
	@ApiParam(hidden = true, defaultValue = "true")
	private Boolean enabled;
	@ApiParam(hidden = true)
	private CompanyType companyType;
	private List<Long> id;

}
