package com.emoldino.api.asset.resource.composite.wgttolopr;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.asset.resource.composite.wgttolopr.dto.WgtTolOprGetIn;
import com.emoldino.api.asset.resource.composite.wgttolopr.dto.WgtTolOprGetOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Asset Management / Widget / Toolings' Operational Summary")
@RequestMapping("/api/asset/wgt-tol-opr")
public interface WgtTolOprController {
//	public static final String NAME = "Widget Tooling's Operational Summary";
	public static final String NAME_PLURAL = "Widget Toolings' Operational Summary";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	WgtTolOprGetOut get(WgtTolOprGetIn input);

}
