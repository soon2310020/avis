package com.emoldino.api.asset.resource.composite.wgttoleol;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.asset.resource.composite.wgttoleol.dto.WgtTolEolGetIn;
import com.emoldino.api.asset.resource.composite.wgttoleol.dto.WgtTolEolGetOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Asset Management / Widget / Tooling's End of Life")
@RequestMapping("/api/asset/wgt-tol-eol")
public interface WgtTolEolController {
//	public static final String NAME = "Widget Tooling's End of Life";
	public static final String NAME_PLURAL = "Widget Toolings' End of Life";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	WgtTolEolGetOut get(WgtTolEolGetIn input);

}
