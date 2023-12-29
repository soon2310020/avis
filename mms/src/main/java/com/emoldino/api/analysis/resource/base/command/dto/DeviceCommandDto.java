package com.emoldino.api.analysis.resource.base.command.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceCommandDto {
	@ApiModelProperty(value = "Device ID", required = true)
	private String deviceId;
	@ApiModelProperty(value = "Device Type", required = true)
	private String deviceType;
	@ApiModelProperty(value = "Command", required = true)
	private String command;
	@ApiModelProperty(value = "Data")
	private String data;
	@ApiModelProperty(value = "Comment")
	private String comment;
}
