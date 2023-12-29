package com.emoldino.api.integration.resource.composite.mfewrk;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.integration.resource.composite.mfewrk.dto.AiMfeResultIn;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Integration / AI Model - Mold Feature Extract Work")
@RequestMapping("/api/integration/mfe-wrk")
public interface AiMfeWrkController {
	String NAME = "Mold Feature Extract";

	@ApiOperation("Post AI Result Manually " + NAME)
	@PostMapping("/mfe/result")
	SuccessOut mfeWorkResult(@RequestBody AiMfeResultIn reqIn);

	@ApiOperation("Post AI Launch Manually " + NAME)
	@GetMapping("mfe/work")
	SuccessOut mfeWorkLaunch();

}
