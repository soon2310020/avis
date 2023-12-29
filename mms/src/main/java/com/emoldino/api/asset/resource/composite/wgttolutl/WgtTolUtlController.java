package com.emoldino.api.asset.resource.composite.wgttolutl;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.asset.resource.composite.wgttolutl.dto.WgtTolUtlGetIn;
import com.emoldino.api.asset.resource.composite.wgttolutl.dto.WgtTolUtlGetOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Asset Management / Widget / Toolings' Overall Utilization")
@RequestMapping("/api/asset/wgt-tol-utl")
public interface WgtTolUtlController {
//	public static final String NAME = "Widget Tooling's Overall Utilization";
	public static final String NAME_PLURAL = "Widget Toolings' Overall Utilization";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	WgtTolUtlGetOut get(WgtTolUtlGetIn input);

}
