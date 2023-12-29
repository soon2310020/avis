package com.emoldino.api.integration.resource.composite.pqwrk;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.integration.resource.composite.pqwrk.dto.AiPqResultIn;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Integration / AI Model - Part Quality Work")
@RequestMapping("/api/integration/pq-wrk")
public interface AiPqWrkController {
	String NAME = "Part Quality";

	@ApiOperation("Post AI Result Manually " + NAME)
	@PostMapping("/result")
	SuccessOut pqWorkResult(@RequestBody AiPqResultIn reqIn);

	@ApiOperation("Post AI Launch Manually " + NAME)
	@GetMapping("/work")
	SuccessOut pqWorkLaunch();
	
}
