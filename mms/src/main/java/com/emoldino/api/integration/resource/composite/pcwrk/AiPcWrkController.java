package com.emoldino.api.integration.resource.composite.pcwrk;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.integration.resource.composite.pcwrk.dto.AiPcResultIn;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Integration / AI Model - Process Change Work")
@RequestMapping("/api/integration/pc-wrk")
public interface AiPcWrkController {
	String NAME = "Process Change";

	@ApiOperation("Post AI Result Manually " + NAME)
	@PostMapping("/result")
	SuccessOut pcWorkResult(@RequestBody AiPcResultIn reqIn);

	@ApiOperation("Post AI Launch Manually " + NAME)
	@GetMapping("/work")
	SuccessOut pcWorkLaunch();

}
