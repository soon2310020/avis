package com.emoldino.api.analysis.resource.composite.mldcht;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.analysis.resource.composite.mldcht.dto.get.MldChtGetIn;
import com.emoldino.api.analysis.resource.composite.mldcht.dto.get.MldChtGetOut;
import com.emoldino.api.analysis.resource.composite.mldcht.dto.getoptions.MldChtGetOptionsIn;
import com.emoldino.api.analysis.resource.composite.mldcht.dto.getoptions.MldChtGetOptionsOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Analysis / Mold Chart")
@RequestMapping("/api/analysis/mld-cht")
public interface MldChtController {

	@ApiOperation("Get Mold Chart Data - Shot Count, Cycle Time,...")
	@GetMapping
	MldChtGetOut get(MldChtGetIn input);

	@ApiOperation("Get Mold Chart Options")
	@GetMapping("/options")
	MldChtGetOptionsOut getOptions(MldChtGetOptionsIn input);

}
