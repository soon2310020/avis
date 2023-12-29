package com.emoldino.api.asset.resource.composite.wgttol;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.asset.resource.composite.wgttol.dto.WgtTolGetIn;
import com.emoldino.api.asset.resource.composite.wgttol.dto.WgtTolGetOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Asset Management / Widget / Total Toolings")
@RequestMapping("/api/asset/wgt-tol")
public interface WgtTolController {
//	public static final String NAME = "Widget Total Tooling";
	public static final String NAME_PLURAL = "Widget Total Toolings";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	WgtTolGetOut get(WgtTolGetIn input);

}
