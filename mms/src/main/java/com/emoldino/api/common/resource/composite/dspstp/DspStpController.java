package com.emoldino.api.common.resource.composite.dspstp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.common.resource.composite.dspstp.dto.DspStpGetOut;
import com.emoldino.api.common.resource.composite.dspstp.dto.DspStpPostIn;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Common / Display Config")
@RequestMapping("/api/common/dsp-stp")
public interface DspStpController {
	static final String NAME_PLURAL = "Display Configs";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	DspStpGetOut get();

	@ApiOperation("Post " + NAME_PLURAL)
	@PostMapping
	SuccessOut post(@RequestBody DspStpPostIn input);

}
