package com.emoldino.api.common.resource.composite.ipc.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class IpcAppVersionsIn {
	@ApiModelProperty(value = "Application Code", required = true, example = "MMS")
	private String appCode;
	private String updatedAtStrGt;
}
