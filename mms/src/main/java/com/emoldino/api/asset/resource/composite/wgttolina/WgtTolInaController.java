package com.emoldino.api.asset.resource.composite.wgttolina;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.asset.resource.composite.wgttolina.dto.WgtTolInaGetIn;
import com.emoldino.api.asset.resource.composite.wgttolina.dto.WgtTolInaGetOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Asset Management / Widget / Inactive Toolings")
@RequestMapping("/api/asset/wgt-tol-ina")
public interface WgtTolInaController {
//	public static final String NAME = "Widget Inactive Tooling";
	public static final String NAME_PLURAL = "Widget Inactive Toolings";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	WgtTolInaGetOut get(WgtTolInaGetIn input);

}
