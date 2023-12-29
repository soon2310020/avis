package com.emoldino.api.asset.resource.composite.tolinastp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.common.resource.base.option.dto.InactiveConfig;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Asset / Inactive Tooling Config")
@RequestMapping("/api/asset/tol-ina-stp")
public interface TolInaStpController {
//	static final String NAME = "Inactive Tooling Config";
	static final String NAME_PLURAL = "Inactive Toolings Config";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	InactiveConfig get();

	@ApiOperation("Post " + NAME_PLURAL)
	@PostMapping
	SuccessOut post(@RequestBody InactiveConfig input);

}
