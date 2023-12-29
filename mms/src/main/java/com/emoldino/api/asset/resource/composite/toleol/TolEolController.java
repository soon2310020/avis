package com.emoldino.api.asset.resource.composite.toleol;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.asset.resource.composite.toleol.dto.TolEolGetIn;
import com.emoldino.api.asset.resource.composite.toleol.dto.TolEolGetOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Asset Management / Tooling End of Life")
@RequestMapping("/api/asset/tol-eol")
public interface TolEolController {
//	static final String NAME = "Tooling End of Life";
	static final String NAME_PLURAL = "Toolings' End of Life";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	TolEolGetOut get(TolEolGetIn input, Pageable pageable);

}
