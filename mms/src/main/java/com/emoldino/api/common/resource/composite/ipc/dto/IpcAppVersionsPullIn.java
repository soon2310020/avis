package com.emoldino.api.common.resource.composite.ipc.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class IpcAppVersionsPullIn {
	@ApiModelProperty(value = "Application Code", required = true, example = "MMS")
	private String appCode;
}
