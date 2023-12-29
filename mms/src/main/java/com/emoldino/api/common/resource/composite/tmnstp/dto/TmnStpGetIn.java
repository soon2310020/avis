package com.emoldino.api.common.resource.composite.tmnstp.dto;

import java.util.List;

import com.emoldino.api.common.resource.composite.tmnstp.enumeration.TmnStpConnectionStatus;

import io.swagger.annotations.ApiParam;
import lombok.Data;
import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.EquipmentStatus;

@Data
public class TmnStpGetIn {
	private String query;
	private String filterCode;

	private String tabName;
	@ApiParam(hidden = true, defaultValue = "true")
	private Boolean enabled;
	@ApiParam(hidden = true)
	private CompanyType companyType;

	private List<TmnStpConnectionStatus> connectionStatus;
	private List<EquipmentStatus> equipmentStatus;
	private List<Long> areaId;
	private List<Long> id;

}
