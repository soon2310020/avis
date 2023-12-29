package com.emoldino.api.asset.resource.composite.wgttolrlo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.asset.resource.composite.wgttolrlo.dto.WgtTolRloGetOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Asset Management / Widget / Tooling's Relocations")
@RequestMapping("/api/asset/wgt-tol-rlo")
public interface WgtTolRloController {
//	public static final String NAME = "Widget Tooling's Relocation";
	public static final String NAME_PLURAL = "Widget Toolings' Relocations";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	WgtTolRloGetOut get();

}
