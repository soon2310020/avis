package com.emoldino.api.asset.resource.composite.wgttoltco;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.asset.resource.composite.wgttoltco.dto.WgtTolTcoGetIn;
import com.emoldino.api.asset.resource.composite.wgttoltco.dto.WgtTolTcoGetOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Asset Management / Widget / Tooling's Total Cost of Ownership (TCO)")
@RequestMapping("/api/asset/wgt-tol-tco")
public interface WgtTolTcoController {
//	public static final String NAME = "Widget Tooling's Total Cost of Ownership (TCO)";
	public static final String NAME_PLURAL = "Widget Toolings' Total Cost of Ownership (TCO)";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	WgtTolTcoGetOut get(WgtTolTcoGetIn input);

}
