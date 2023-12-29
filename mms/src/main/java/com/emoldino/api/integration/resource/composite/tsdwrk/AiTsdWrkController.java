package com.emoldino.api.integration.resource.composite.tsdwrk;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.integration.resource.composite.tsdwrk.dto.AiTsdResultIn;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Integration / AI Model - Test Shot Detection Work")
@RequestMapping("/api/integration/tsd-wrk")
public interface AiTsdWrkController {
	String NAME = "Test Shot Detection";

	@ApiOperation("Post AI Result Manually " + NAME)
	@PostMapping("/tsd/result")
	SuccessOut tsdWorkResult(@RequestBody AiTsdResultIn reqIn);

	@ApiOperation("Post AI Launch Manually " + NAME)
	@GetMapping("/tsd/work")
	SuccessOut tsdWorkLaunch();

}
