package com.emoldino.api.common.resource.composite.comstp.dto;

import java.util.List;

import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.CompanyType;

@Data
@NoArgsConstructor
public class ComStpGetIn {
	private String filterCode;
	private String tabName;
	private String query;

	@ApiParam(hidden = true)
	private CompanyType companyType;
	@ApiParam(hidden = true, defaultValue = "true")
	private Boolean enabled;
	private List<Long> id;

}
