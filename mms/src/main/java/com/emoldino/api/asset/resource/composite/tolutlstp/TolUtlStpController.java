package com.emoldino.api.asset.resource.composite.tolutlstp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.common.resource.base.option.dto.UtilizationConfig;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Asset / Tooling Utilization Config")
@RequestMapping("/api/asset/tol-utl-stp")
public interface TolUtlStpController {
//	static final String NAME = "Tooling Utilization Config";
	static final String NAME_PLURAL = "Toolings Utilization Config";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	UtilizationConfig get();

	@ApiOperation("Post " + NAME_PLURAL)
	@PostMapping
	SuccessOut post(@RequestBody UtilizationConfig input);

}
