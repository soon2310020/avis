package com.emoldino.api.analysis.resource.base.command;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.analysis.resource.base.command.dto.DeviceCommandDto;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Analysis / Command")
@RequestMapping("/api/analysis/commands")
public interface CommandController {

	@ApiOperation("Post Device Command")
	@PostMapping("/device")
	SuccessOut postDevice(DeviceCommandDto data);

}
