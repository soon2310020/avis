package com.emoldino.api.production.resource.composite.oeestp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.production.resource.composite.oeestp.dto.OeeStpGetOut;
import com.emoldino.api.production.resource.composite.oeestp.dto.OeeStpPostIn;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Production / OEE Config")
@RequestMapping("/api/production/oee-stp")
public interface OeeStpController {
//	static final String NAME = "OEE Config";
	static final String NAME_PLURAL = "OEE Configs";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	OeeStpGetOut get();

	@ApiOperation("Post " + NAME_PLURAL)
	@PostMapping
	SuccessOut post(@RequestBody OeeStpPostIn input);

}
