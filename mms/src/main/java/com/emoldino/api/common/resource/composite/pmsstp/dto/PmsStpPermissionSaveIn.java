package com.emoldino.api.common.resource.composite.pmsstp.dto;

import java.util.List;

import com.emoldino.api.common.resource.composite.pmsstp.enumeration.PmsStpResourceTypeEnum;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PmsStpPermissionSaveIn {
	@ApiModelProperty(value = "Resource Type", required = true)
	private PmsStpResourceTypeEnum resourceType;
	List<PmsStpPermission> content;
}
